package br.com.gopro.api.config;

import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.enums.DocumentOwnerTypeEnum;
import br.com.gopro.api.model.Document;
import br.com.gopro.api.repository.*;
import br.com.gopro.api.service.AuditLogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
public class ApiMutationAuditFilter extends OncePerRequestFilter {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private static final Set<String> SKIPPED_PATH_PREFIXES = Set.of(
            "/admin/",
            "/audit-log",
            "/auth/login",
            "/register",
            "/v3/api-docs",
            "/swagger-ui",
            "/health",
            "/error"
    );

    private static final Set<String> CONTRACT_RESOURCES = Set.of(
            "projects",
            "budget-categories",
            "budget-items",
            "budget-transfers",
            "disbursement-schedules",
            "goals",
            "stages",
            "phases",
            "incomes",
            "expenses",
            "project-people",
            "project-companies",
            "project-organizations",
            "project_organization",
            "documents"
    );

    private static final Set<String> PEOPLE_COMPANY_RESOURCES = Set.of(
            "peoples",
            "companies",
            "organizations",
            "partners",
            "public-agencies",
            "secretaries"
    );

    private static final Set<DocumentOwnerTypeEnum> PEOPLE_COMPANY_OWNER_TYPES = EnumSet.of(
            DocumentOwnerTypeEnum.PEOPLE,
            DocumentOwnerTypeEnum.COMPANY,
            DocumentOwnerTypeEnum.ORGANIZATION,
            DocumentOwnerTypeEnum.PARTNER,
            DocumentOwnerTypeEnum.PUBLIC_AGENCY,
            DocumentOwnerTypeEnum.SECRETARY
    );

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    private final BudgetCategoryRepository budgetCategoryRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final BudgetTransferRepository budgetTransferRepository;
    private final DisbursementScheduleRepository disbursementScheduleRepository;
    private final GoalRepository goalRepository;
    private final StageRepository stageRepository;
    private final PhaseRepository phaseRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final ProjectPeopleRepository projectPeopleRepository;
    private final ProjectCompanyRepository projectCompanyRepository;
    private final DocumentRepository documentRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        if (!MUTATING_METHODS.contains(method)) {
            return true;
        }

        String path = normalizePath(request);
        for (String skippedPrefix : SKIPPED_PATH_PREFIXES) {
            if (path.startsWith(skippedPrefix)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        HttpServletRequest requestToUse = request;
        if (!isMultipartRequest(request) && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request);
        }

        filterChain.doFilter(requestToUse, response);

        if (response.getStatus() >= 400) {
            return;
        }

        Long actorUserId = resolveActorUserId();
        if (actorUserId == null) {
            return;
        }

        try {
            AuditMutationContext context = resolveContext(requestToUse);
            String action = "API_" + request.getMethod().toUpperCase();
            String entityType = context.scope().prefix() + ":" + context.resource();

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("scope", context.scope().name());
            payload.put("resource", context.resource());
            payload.put("path", normalizePath(requestToUse));
            payload.put("method", request.getMethod().toUpperCase());
            payload.put("status", response.getStatus());
            if (context.contractId() != null) {
                payload.put("contractId", context.contractId());
            }
            if (context.ownerType() != null) {
                payload.put("ownerType", context.ownerType().name());
                payload.put("ownerId", context.ownerId());
            }

            auditLogService.log(
                    actorUserId,
                    action,
                    entityType,
                    context.entityId(),
                    null,
                    payload,
                    requestToUse
            );
        } catch (Exception ex) {
            log.debug("mutation_audit_failed path={} reason={}", normalizePath(requestToUse), ex.getMessage());
        }
    }

    private AuditMutationContext resolveContext(HttpServletRequest request) {
        String path = normalizePath(request);
        String[] segments = path.split("/");
        String baseResource = segments.length > 1 ? normalizeResourceName(segments[1]) : "unknown";
        String idSegment = segments.length > 2 ? segments[2] : null;

        JsonNode body = parseJsonBody(request);
        OwnerReference ownerReference = resolveOwnerReference(request, baseResource, idSegment, body);
        Long contractId = resolveContractId(request, baseResource, idSegment, body, ownerReference);
        AuditScopeEnum scope = resolveScope(baseResource, ownerReference, contractId);
        String resource = buildResourceLabel(baseResource, ownerReference);
        String entityId = resolveEntityId(scope, contractId, idSegment);

        return new AuditMutationContext(scope, resource, contractId, entityId, ownerReference);
    }

    private String buildResourceLabel(String baseResource, OwnerReference ownerReference) {
        if (!"documents".equals(baseResource) || ownerReference == null) {
            return baseResource;
        }
        return "documents." + ownerReference.ownerType().name().toLowerCase();
    }

    private String resolveEntityId(AuditScopeEnum scope, Long contractId, String idSegment) {
        if (scope == AuditScopeEnum.CONTRACTS && contractId != null) {
            return String.valueOf(contractId);
        }
        if (idSegment == null || idSegment.isBlank()) {
            return null;
        }
        return idSegment;
    }

    private AuditScopeEnum resolveScope(String baseResource, OwnerReference ownerReference, Long contractId) {
        if ("documents".equals(baseResource)) {
            if (contractId != null) {
                return AuditScopeEnum.CONTRACTS;
            }
            if (ownerReference != null && PEOPLE_COMPANY_OWNER_TYPES.contains(ownerReference.ownerType())) {
                return AuditScopeEnum.PEOPLE_COMPANIES;
            }
            return AuditScopeEnum.SYSTEM;
        }

        if (CONTRACT_RESOURCES.contains(baseResource)) {
            return AuditScopeEnum.CONTRACTS;
        }

        if (PEOPLE_COMPANY_RESOURCES.contains(baseResource)) {
            return AuditScopeEnum.PEOPLE_COMPANIES;
        }

        return AuditScopeEnum.SYSTEM;
    }

    private Long resolveContractId(
            HttpServletRequest request,
            String baseResource,
            String idSegment,
            JsonNode body,
            OwnerReference ownerReference
    ) {
        Long byQuery = parseLong(request.getParameter("projectId"));
        if (byQuery != null) {
            return byQuery;
        }

        Long byBody = extractLong(body, "projectId");
        if (byBody != null) {
            return byBody;
        }

        switch (baseResource) {
            case "projects":
                return parseLong(idSegment);
            case "budget-categories": {
                Long id = parseLong(idSegment);
                if (id != null) return budgetCategoryRepository.findProjectIdById(id).orElse(null);
                return null;
            }
            case "budget-items": {
                Long id = parseLong(idSegment);
                if (id != null) return budgetItemRepository.findProjectIdById(id).orElse(null);

                Long categoryId = extractLong(body, "categoryId");
                if (categoryId != null) {
                    return budgetCategoryRepository.findProjectIdById(categoryId).orElse(null);
                }
                return null;
            }
            case "budget-transfers": {
                Long id = parseLong(idSegment);
                if (id != null) return budgetTransferRepository.findProjectIdById(id).orElse(null);

                Long fromItemId = extractLong(body, "fromItemId");
                if (fromItemId != null) {
                    return budgetItemRepository.findProjectIdById(fromItemId).orElse(null);
                }
                return null;
            }
            case "disbursement-schedules": {
                Long id = parseLong(idSegment);
                if (id != null) return disbursementScheduleRepository.findProjectIdById(id).orElse(null);
                return null;
            }
            case "goals": {
                Long id = parseLong(idSegment);
                if (id != null) return goalRepository.findProjectIdById(id).orElse(null);
                return null;
            }
            case "stages": {
                Long id = parseLong(idSegment);
                if (id != null) return stageRepository.findProjectIdById(id).orElse(null);

                Long goalId = extractLong(body, "goalId");
                if (goalId != null) {
                    return goalRepository.findProjectIdById(goalId).orElse(null);
                }
                return null;
            }
            case "phases": {
                Long id = parseLong(idSegment);
                if (id != null) return phaseRepository.findProjectIdById(id).orElse(null);

                Long stageId = extractLong(body, "stageId");
                if (stageId != null) {
                    return stageRepository.findProjectIdById(stageId).orElse(null);
                }
                return null;
            }
            case "incomes": {
                Long id = parseLong(idSegment);
                if (id != null) return incomeRepository.findProjectIdById(id).orElse(null);
                return null;
            }
            case "expenses": {
                Long id = parseLong(idSegment);
                if (id != null) return expenseRepository.findProjectIdById(id).orElse(null);

                Long incomeId = extractLong(body, "incomeId");
                if (incomeId != null) {
                    return incomeRepository.findProjectIdById(incomeId).orElse(null);
                }
                Long budgetItemId = extractLong(body, "budgetItemId");
                if (budgetItemId != null) {
                    return budgetItemRepository.findProjectIdById(budgetItemId).orElse(null);
                }
                return null;
            }
            case "project-people": {
                Long id = parseLong(idSegment);
                if (id != null) return projectPeopleRepository.findProjectIdById(id).orElse(null);
                return null;
            }
            case "project-companies":
            case "project-organizations":
            case "project_organization": {
                Long id = parseLong(idSegment);
                if (id != null) return projectCompanyRepository.findProjectIdById(id).orElse(null);
                return null;
            }
            case "documents":
                if (ownerReference != null) {
                    return resolveContractIdFromOwner(ownerReference);
                }
                return null;
            default:
                return null;
        }
    }

    private OwnerReference resolveOwnerReference(
            HttpServletRequest request,
            String baseResource,
            String idSegment,
            JsonNode body
    ) {
        if (!"documents".equals(baseResource)) {
            return null;
        }

        if (idSegment != null && !idSegment.isBlank()) {
            try {
                UUID documentId = UUID.fromString(idSegment);
                Optional<Document> document = documentRepository.findById(documentId);
                if (document.isPresent()) {
                    return new OwnerReference(document.get().getOwnerType(), document.get().getOwnerId());
                }
            } catch (IllegalArgumentException ignored) {
                // not a UUID path segment
            }
        }

        String ownerTypeText = Optional.ofNullable(request.getParameter("ownerType"))
                .orElseGet(() -> extractText(body, "ownerType"));
        Long ownerId = Optional.ofNullable(parseLong(request.getParameter("ownerId")))
                .orElseGet(() -> extractLong(body, "ownerId"));
        DocumentOwnerTypeEnum ownerType = parseOwnerType(ownerTypeText);
        if (ownerType == null || ownerId == null) {
            return null;
        }
        return new OwnerReference(ownerType, ownerId);
    }

    private Long resolveContractIdFromOwner(OwnerReference ownerReference) {
        Long ownerId = ownerReference.ownerId();
        return switch (ownerReference.ownerType()) {
            case PROJECT -> ownerId;
            case BUDGET_ITEM -> budgetItemRepository.findProjectIdById(ownerId).orElse(null);
            case BUDGET_TRANSFER -> budgetTransferRepository.findProjectIdById(ownerId).orElse(null);
            case INCOME -> incomeRepository.findProjectIdById(ownerId).orElse(null);
            case EXPENSE -> expenseRepository.findProjectIdById(ownerId).orElse(null);
            case GOAL -> goalRepository.findProjectIdById(ownerId).orElse(null);
            case STAGE -> stageRepository.findProjectIdById(ownerId).orElse(null);
            case PHASE -> phaseRepository.findProjectIdById(ownerId).orElse(null);
            case PROJECT_PEOPLE -> projectPeopleRepository.findProjectIdById(ownerId).orElse(null);
            case PROJECT_COMPANY -> projectCompanyRepository.findProjectIdById(ownerId).orElse(null);
            default -> null;
        };
    }

    private String normalizeResourceName(String resource) {
        if (resource == null || resource.isBlank()) {
            return "unknown";
        }
        return resource.trim();
    }

    private JsonNode parseJsonBody(HttpServletRequest request) {
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
            return null;
        }
        if (!isJsonRequest(request)) {
            return null;
        }
        byte[] bodyBytes = wrapper.getContentAsByteArray();
        if (bodyBytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readTree(new String(bodyBytes, StandardCharsets.UTF_8));
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    private Long resolveActorUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUserPrincipal authPrincipal) {
            return authPrincipal.id();
        }
        return null;
    }

    private String normalizePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null || path.isBlank()) {
            return "/";
        }
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    private Long parseLong(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long extractLong(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) {
            return null;
        }
        JsonNode node = body.get(field);
        if (node.isNumber()) {
            return node.longValue();
        }
        if (node.isTextual()) {
            return parseLong(node.asText());
        }
        return null;
    }

    private String extractText(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) {
            return null;
        }
        JsonNode node = body.get(field);
        if (node.isTextual()) {
            return node.asText();
        }
        return node.toString();
    }

    private DocumentOwnerTypeEnum parseOwnerType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return DocumentOwnerTypeEnum.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private record OwnerReference(DocumentOwnerTypeEnum ownerType, Long ownerId) {
    }

    private record AuditMutationContext(
            AuditScopeEnum scope,
            String resource,
            Long contractId,
            String entityId,
            OwnerReference ownerReference
    ) {
        DocumentOwnerTypeEnum ownerType() {
            return ownerReference != null ? ownerReference.ownerType() : null;
        }

        Long ownerId() {
            return ownerReference != null ? ownerReference.ownerId() : null;
        }
    }
}
