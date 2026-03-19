package br.com.gopro.api.config;

import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.DocumentOwnerTypeEnum;
import br.com.gopro.api.model.Document;
import br.com.gopro.api.repository.*;
import br.com.gopro.api.service.AuditLogService;
import br.com.gopro.api.service.audit.AuditEventRequest;
import br.com.gopro.api.service.audit.AuditFieldChange;
import br.com.gopro.api.service.audit.ContractAuditDeltaResolver;
import br.com.gopro.api.service.audit.AuditSnapshotExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Map.entry;

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

    private static final Map<String, String> PROJECT_FIELD_LABELS = Map.ofEntries(
            entry("name", "Nome do projeto"),
            entry("code", "Codigo do projeto"),
            entry("object", "Objeto do projeto"),
            entry("primaryPartnerId", "Parceiro primario"),
            entry("secundaryPartnerId", "Parceiro secundario"),
            entry("primaryClientId", "Cliente primario"),
            entry("secundaryClientId", "Cliente secundario"),
            entry("cordinatorId", "Coordenador"),
            entry("projectGovIf", "Unidade GOV/IF"),
            entry("projectType", "Tipo do projeto"),
            entry("projectStatus", "Status do projeto"),
            entry("contractValue", "Valor do contrato"),
            entry("startDate", "Data de inicio"),
            entry("endDate", "Data de termino"),
            entry("openingDate", "Data de abertura"),
            entry("closingDate", "Data de encerramento"),
            entry("city", "Cidade"),
            entry("state", "Estado"),
            entry("executionLocation", "Local de execucao"),
            entry("executedByInnovatis", "Execucao pela Innovatis"),
            entry("areaSegmento", "Area/segmento")
    );

    private static final Map<String, String> PROJECT_FIELD_ACTION_PHRASES = Map.ofEntries(
            entry("name", "o nome do projeto"),
            entry("code", "o codigo do projeto"),
            entry("object", "o objeto do projeto"),
            entry("primaryPartnerId", "o parceiro primario"),
            entry("secundaryPartnerId", "o parceiro secundario"),
            entry("primaryClientId", "o cliente primario"),
            entry("secundaryClientId", "o cliente secundario"),
            entry("cordinatorId", "o coordenador"),
            entry("projectGovIf", "a unidade GOV/IF"),
            entry("projectType", "o tipo do projeto"),
            entry("projectStatus", "o status do projeto"),
            entry("contractValue", "o valor do contrato"),
            entry("startDate", "a data de inicio"),
            entry("endDate", "a data de termino"),
            entry("openingDate", "a data de abertura"),
            entry("closingDate", "a data de encerramento"),
            entry("city", "a cidade"),
            entry("state", "o estado"),
            entry("executionLocation", "o local de execucao"),
            entry("executedByInnovatis", "a execucao pela Innovatis"),
            entry("areaSegmento", "a area/segmento")
    );

    private static final Map<String, String> RESOURCE_LABELS = Map.ofEntries(
            entry("projects", "projeto"),
            entry("documents", "arquivo"),
            entry("budget-categories", "rubrica"),
            entry("budget-items", "item de rubrica"),
            entry("budget-transfers", "remanejamento"),
            entry("disbursement-schedules", "desembolso"),
            entry("goals", "meta"),
            entry("stages", "etapa"),
            entry("phases", "fase"),
            entry("incomes", "receita"),
            entry("expenses", "despesa"),
            entry("project-people", "vinculo de pessoa"),
            entry("project-companies", "vinculo de empresa"),
            entry("project-organizations", "vinculo de organizacao"),
            entry("project_organization", "vinculo de organizacao")
    );

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final AuditSnapshotExtractor auditSnapshotExtractor;
    private final ContractAuditDeltaResolver contractAuditDeltaResolver;

    private final ProjectRepository projectRepository;
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

        ContentCachingResponseWrapper responseToUse = new ContentCachingResponseWrapper(response);

        try {
            String idSegment = resolveIdSegment(requestToUse);
            String baseResource = resolveBaseResource(requestToUse);
            String method = requestToUse.getMethod().toUpperCase(Locale.ROOT);
            Map<String, Object> beforeSnapshot = resolveSnapshot(baseResource, idSegment);

            filterChain.doFilter(requestToUse, responseToUse);

            if (responseToUse.getStatus() >= 400) {
                return;
            }

            Long actorUserId = resolveActorUserId();
            if (actorUserId == null) {
                return;
            }

            try {
                JsonNode requestBody = parseJsonBody(requestToUse);
                JsonNode responseBody = parseJsonResponse(responseToUse);

                AuditMutationContext context = resolveContext(requestToUse, requestBody);
                AuditActionDescription actionDescription = buildActionDescription(
                        requestToUse,
                        context,
                        requestBody,
                        responseBody
                );

                String entityType = context.scope().prefix() + ":" + context.resource();
                Map<String, Object> payload = buildPayload(
                        requestToUse,
                        responseToUse,
                        context,
                        actionDescription
                );

                Map<String, Object> afterSnapshot = resolveAfterSnapshot(
                        method,
                        baseResource,
                        idSegment,
                        responseBody,
                        payload
                );

                String actionCode = resolveActionCode(method);
                String resourceKey = normalizeResourceKey(context.resource());
                AuditScopeEnum effectiveScope = normalizeScope(context.scope(), resourceKey);
                String modulo = resolveModulo(context.scope(), resourceKey);
                String feature = resolveFeature(resourceKey, actionCode);
                String entidadePrincipal = resolveEntidadePrincipal(context.scope(), resourceKey);
                String aba = resolveAba(context.scope(), resourceKey);
                String subsecao = resolveSubsecao(resourceKey);
                List<AuditFieldChange> alteracoes = null;
                if (effectiveScope == AuditScopeEnum.CONTRACTS) {
                    ContractAuditDeltaResolver.ContractAuditDelta contractDelta = contractAuditDeltaResolver.resolve(
                            resourceKey,
                            actionCode,
                            beforeSnapshot,
                            afterSnapshot
                    );
                    alteracoes = contractDelta.changes();
                    payload.put("deltaReliable", contractDelta.reliable());
                    payload.put("deltaSource", contractDelta.source());
                    payload.put("skipAutomaticDelta", true);
                    if (!contractDelta.changes().isEmpty()) {
                        payload.put("changes", contractDelta.changes());
                    } else {
                        payload.remove("changes");
                    }
                    if (!actionDescription.changedFields().isEmpty()) {
                        payload.put("requestFields", actionDescription.changedFields());
                    }
                } else if (!actionDescription.changedFields().isEmpty()) {
                    alteracoes = actionDescription.changedFields().stream()
                            .map(change -> new AuditFieldChange(change.label(), null, change.value(), "EDITADO"))
                            .toList();
                }

                Map<String, Object> detalhesTecnicos = new LinkedHashMap<>(payload);
                detalhesTecnicos.put("entityTypeLegacy", entityType);
                if (context.contractId() != null) {
                    detalhesTecnicos.put("contractId", context.contractId());
                }

                auditLogService.log(
                        AuditEventRequest.builder()
                                .actorUserId(actorUserId)
                                .tipoAuditoria(effectiveScope)
                                .modulo(modulo)
                                .feature(feature)
                                .entidadePrincipal(entidadePrincipal)
                                .entidadeId(context.entityId())
                                .aba(aba)
                                .subsecao(subsecao)
                                .acao(actionCode)
                                .resultado(AuditResultEnum.SUCESSO)
                                .antes(beforeSnapshot)
                                .depois(afterSnapshot)
                                .alteracoes(alteracoes)
                                .detalhesTecnicos(detalhesTecnicos)
                                .build(),
                        requestToUse
                );
            } catch (Exception ex) {
                log.debug("mutation_audit_failed path={} reason={}", normalizePath(requestToUse), ex.getMessage());
            }
        } finally {
            responseToUse.copyBodyToResponse();
        }
    }

    private String resolveBaseResource(HttpServletRequest request) {
        String path = normalizePath(request);
        String[] segments = path.split("/");
        return segments.length > 1 ? normalizeResourceName(segments[1]) : "unknown";
    }

    private String resolveIdSegment(HttpServletRequest request) {
        String path = normalizePath(request);
        String[] segments = path.split("/");
        return segments.length > 2 ? segments[2] : null;
    }

    private Map<String, Object> resolveSnapshot(String baseResource, String idSegment) {
        if (idSegment == null || idSegment.isBlank()) {
            return null;
        }
        try {
            return switch (baseResource) {
                case "projects" -> projectRepository.findById(parseLong(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                case "budget-categories" -> budgetCategoryRepository.findById(parseLong(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                case "budget-items" -> budgetItemRepository.findById(parseLong(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                case "budget-transfers" -> budgetTransferRepository.findById(parseLong(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                case "disbursement-schedules" -> disbursementScheduleRepository.findById(parseLong(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                case "goals" -> goalRepository.findById(parseLong(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                case "stages" -> stageRepository.findById(parseLong(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                case "phases" -> phaseRepository.findById(parseLong(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                case "incomes" -> incomeRepository.findById(parseLong(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                case "expenses" -> expenseRepository.findById(parseLong(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                case "project-people" -> projectPeopleRepository.findById(parseLong(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                case "project-companies", "project-organizations", "project_organization" ->
                        projectCompanyRepository.findById(parseLong(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                case "documents" -> {
                    try {
                        yield documentRepository.findById(UUID.fromString(idSegment)).map(auditSnapshotExtractor::extract).orElse(null);
                    } catch (IllegalArgumentException ignored) {
                        yield null;
                    }
                }
                default -> null;
            };
        } catch (Exception ignored) {
            return null;
        }
    }

    private Map<String, Object> resolveAfterSnapshot(
            String method,
            String baseResource,
            String idSegment,
            JsonNode responseBody,
            Map<String, Object> payload
    ) {
        if ("DELETE".equals(method)) {
            return null;
        }

        String recordId = idSegment;
        if ((recordId == null || recordId.isBlank()) && responseBody != null) {
            String parsed = extractText(responseBody, "id");
            if (parsed != null && !parsed.isBlank()) {
                recordId = parsed;
            }
        }

        Map<String, Object> fromRepository = resolveSnapshot(baseResource, recordId);
        if (fromRepository != null) {
            return fromRepository;
        }

        if (responseBody != null) {
            return auditSnapshotExtractor.extract(objectMapper.convertValue(responseBody, Map.class));
        }

        return payload;
    }

    private String resolveActionCode(String method) {
        return switch (method) {
            case "POST" -> "CRIAR";
            case "DELETE" -> "EXCLUIR";
            default -> "ATUALIZAR";
        };
    }

    private AuditScopeEnum normalizeScope(AuditScopeEnum original, String resourceKey) {
        if (original == AuditScopeEnum.PEOPLE_COMPANIES) {
            return AuditScopeEnum.SYSTEM;
        }
        if ("users".equals(resourceKey) || resourceKey.startsWith("allowed_registration")) {
            return AuditScopeEnum.USERS;
        }
        return original;
    }

    private String resolveModulo(AuditScopeEnum scope, String resourceKey) {
        AuditScopeEnum effectiveScope = normalizeScope(scope, resourceKey);
        if (effectiveScope == AuditScopeEnum.CONTRACTS) {
            return "Contratos";
        }
        if (effectiveScope == AuditScopeEnum.USERS) {
            return "Usuarios";
        }
        return "Sistema";
    }

    private String resolveFeature(String resourceKey, String actionCode) {
        String resource = RESOURCE_LABELS.getOrDefault(resourceKey, resourceKey);
        if (resource == null || resource.isBlank()) {
            resource = "registro";
        }
        return switch (actionCode) {
            case "CRIAR" -> "Cadastro de " + resource;
            case "EXCLUIR" -> "Exclusao de " + resource;
            default -> "Edicao de " + resource;
        };
    }

    private String resolveEntidadePrincipal(AuditScopeEnum scope, String resourceKey) {
        AuditScopeEnum effectiveScope = normalizeScope(scope, resourceKey);
        if (effectiveScope == AuditScopeEnum.CONTRACTS) {
            return "Contrato";
        }
        if (effectiveScope == AuditScopeEnum.USERS) {
            return "Usuario";
        }
        return "Sistema";
    }

    private String resolveAba(AuditScopeEnum scope, String resourceKey) {
        if (normalizeScope(scope, resourceKey) != AuditScopeEnum.CONTRACTS) {
            return null;
        }
        return switch (resourceKey) {
            case "projects" -> "Contrato";
            case "budget-categories", "budget-items", "budget-transfers" -> "Rubricas";
            case "incomes", "expenses" -> "Pagamentos";
            case "disbursement-schedules" -> "Desembolso";
            case "goals", "stages", "phases" -> "Metas";
            case "project-people" -> "Pessoas";
            case "project-companies", "project-organizations", "project_organization" -> "Empresas";
            case "documents" -> "Arquivos";
            default -> "Contrato";
        };
    }

    private String resolveSubsecao(String resourceKey) {
        return switch (resourceKey) {
            case "budget-categories" -> "Rubricas";
            case "budget-items" -> "Itens de rubrica";
            case "budget-transfers" -> "Remanejamentos";
            case "incomes" -> "Receitas";
            case "expenses" -> "Despesas";
            case "goals" -> "Metas";
            case "stages" -> "Etapas";
            case "phases" -> "Fases";
            case "project-people" -> "Pessoas vinculadas";
            case "project-companies" -> "Empresas vinculadas";
            case "project-organizations", "project_organization" -> "Organizacoes vinculadas";
            case "documents" -> "Arquivos";
            default -> null;
        };
    }

    private String buildResumo(AuditMutationContext context, AuditActionDescription actionDescription, String actionCode) {
        if (context.scope() == AuditScopeEnum.CONTRACTS) {
            String contract = context.entityId() != null ? "Contrato #" + context.entityId() : "Contrato";
            String resourceKey = normalizeResourceKey(context.resource());
            String aba = resolveAba(context.scope(), resourceKey);
            if ("documents".equals(resourceKey) && actionDescription.fileName() != null) {
                String verb = "CRIAR".equals(actionCode) ? "Adicionado anexo" : "EXCLUIR".equals(actionCode) ? "Excluido anexo" : "Atualizado anexo";
                return contract + ": " + verb + " '" + actionDescription.fileName() + "' (Aba " + aba + ")";
            }
            return contract + ": " + actionDescription.action() + (aba != null ? " (Aba " + aba + ")" : "");
        }
        return actionDescription.action();
    }

    private String buildDescricao(String feature, String aba, List<ChangedField> changedFields) {
        StringBuilder description = new StringBuilder();
        description.append("Tela ").append(feature).append(". ");
        if (aba != null && !aba.isBlank()) {
            description.append("Alteracao registrada na aba ").append(aba).append(". ");
        }
        if (changedFields == null || changedFields.isEmpty()) {
            description.append("Sem detalhamento de campos alterados.");
            return description.toString();
        }
        description.append("Campos alterados: ");
        int limit = Math.min(changedFields.size(), 5);
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                description.append(", ");
            }
            description.append(changedFields.get(i).label());
        }
        if (changedFields.size() > limit) {
            description.append(", outros campos");
        }
        description.append(".");
        return description.toString();
    }

    private AuditMutationContext resolveContext(HttpServletRequest request, JsonNode body) {
        String path = normalizePath(request);
        String[] segments = path.split("/");
        String baseResource = segments.length > 1 ? normalizeResourceName(segments[1]) : "unknown";
        String idSegment = segments.length > 2 ? segments[2] : null;

        OwnerReference ownerReference = resolveOwnerReference(request, baseResource, idSegment, body);
        Long contractId = resolveContractId(request, baseResource, idSegment, body, ownerReference);
        AuditScopeEnum scope = resolveScope(baseResource, ownerReference, contractId);
        String resource = buildResourceLabel(baseResource, ownerReference);
        String entityId = resolveEntityId(scope, contractId, idSegment);

        return new AuditMutationContext(scope, resource, contractId, entityId, ownerReference);
    }

    private AuditActionDescription buildActionDescription(
            HttpServletRequest request,
            AuditMutationContext context,
            JsonNode requestBody,
            JsonNode responseBody
    ) {
        String method = request.getMethod().toUpperCase(Locale.ROOT);
        String resourceKey = normalizeResourceKey(context.resource());
        List<ChangedField> changedFields = extractChangedFields(resourceKey, requestBody);

        String fileName = null;
        if ("documents".equals(resourceKey)) {
            fileName = firstNonBlank(
                    extractText(responseBody, "originalName"),
                    context.documentName(),
                    extractMultipartFilename(request)
            );

            if (fileName != null) {
                changedFields = appendChangeIfMissing(changedFields, "originalName", "Nome do arquivo", fileName);
            }

            String category = firstNonBlank(
                    extractText(responseBody, "category"),
                    request.getParameter("category")
            );
            if (category != null) {
                changedFields = appendChangeIfMissing(changedFields, "category", "Categoria", category);
            }
        }

        String actionLabel = resolveActionLabel(method, resourceKey, changedFields);
        return new AuditActionDescription(actionLabel, changedFields, fileName);
    }

    private String resolveActionLabel(String method, String resourceKey, List<ChangedField> changedFields) {
        if ("projects".equals(resourceKey)) {
            return switch (method) {
                case "POST" -> "Criou o projeto";
                case "DELETE" -> "Excluiu o projeto";
                case "PUT", "PATCH" -> "Atualizou o projeto";
                default -> "Alterou o projeto";
            };
        }

        if ("documents".equals(resourceKey)) {
            return switch (method) {
                case "POST" -> "Adicionou o arquivo";
                case "DELETE" -> "Excluiu o arquivo";
                case "PUT", "PATCH" -> "Atualizou o arquivo";
                default -> "Alterou arquivo";
            };
        }

        String resourceLabel = RESOURCE_LABELS.getOrDefault(resourceKey, "registro");
        return switch (method) {
            case "POST" -> "Criou " + resourceLabel;
            case "PUT", "PATCH" -> "Atualizou " + resourceLabel;
            case "DELETE" -> "Excluiu " + resourceLabel;
            default -> "Alterou " + resourceLabel;
        };
    }

    private Map<String, Object> buildPayload(
            HttpServletRequest request,
            HttpServletResponse response,
            AuditMutationContext context,
            AuditActionDescription actionDescription
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scope", context.scope().name());
        payload.put("resource", context.resource());
        payload.put("path", normalizePath(request));
        payload.put("method", request.getMethod().toUpperCase(Locale.ROOT));
        payload.put("status", response.getStatus());
        payload.put("actionLabel", actionDescription.action());

        if (context.contractId() != null) {
            payload.put("contractId", context.contractId());
        }
        if (context.ownerType() != null) {
            payload.put("ownerType", context.ownerType().name());
            payload.put("ownerId", context.ownerId());
        }
        if (actionDescription.fileName() != null) {
            payload.put("fileName", actionDescription.fileName());
        }
        if (!actionDescription.changedFields().isEmpty()) {
            payload.put("changes", actionDescription.changedFields());
        }

        return payload;
    }

    private List<ChangedField> extractChangedFields(String resourceKey, JsonNode body) {
        if (body == null || !body.isObject()) {
            return List.of();
        }

        List<ChangedField> changedFields = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = body.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            if ("createdBy".equals(fieldName) || "updatedBy".equals(fieldName)) {
                continue;
            }

            String label = resolveFieldLabel(resourceKey, fieldName);
            String value = summarizeValue(field.getValue());
            changedFields.add(new ChangedField(fieldName, label, value));
        }

        return changedFields;
    }

    private String resolveFieldLabel(String resourceKey, String fieldName) {
        if ("projects".equals(resourceKey)) {
            return PROJECT_FIELD_LABELS.getOrDefault(fieldName, humanizeFieldName(fieldName));
        }
        return humanizeFieldName(fieldName);
    }

    private String summarizeValue(JsonNode value) {
        if (value == null || value.isNull()) {
            return "vazio";
        }
        if (value.isTextual()) {
            return trimAndLimit(value.asText(), 160);
        }
        if (value.isNumber() || value.isBoolean()) {
            return value.asText();
        }
        if (value.isArray()) {
            return value.size() + " item(ns)";
        }
        if (value.isObject()) {
            return "objeto";
        }
        return trimAndLimit(value.toString(), 160);
    }

    private List<ChangedField> appendChangeIfMissing(
            List<ChangedField> original,
            String field,
            String label,
            String value
    ) {
        for (ChangedField changedField : original) {
            if (field.equals(changedField.field())) {
                return original;
            }
        }
        List<ChangedField> copy = new ArrayList<>(original);
        copy.add(new ChangedField(field, label, value));
        return copy;
    }

    private JsonNode parseJsonResponse(ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("application/json")) {
            return null;
        }

        byte[] bodyBytes = response.getContentAsByteArray();
        if (bodyBytes.length == 0) {
            return null;
        }

        try {
            return objectMapper.readTree(new String(bodyBytes, StandardCharsets.UTF_8));
        } catch (Exception ex) {
            return null;
        }
    }

    private String normalizeResourceKey(String resource) {
        if (resource == null || resource.isBlank()) {
            return "unknown";
        }
        int dotIndex = resource.indexOf('.');
        if (dotIndex < 0) {
            return resource;
        }
        return resource.substring(0, dotIndex);
    }

    private String humanizeFieldName(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return "Campo";
        }
        String withSpaces = fieldName
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replace('_', ' ')
                .trim();
        if (withSpaces.isEmpty()) {
            return "Campo";
        }
        return Character.toUpperCase(withSpaces.charAt(0)) + withSpaces.substring(1).toLowerCase(Locale.ROOT);
    }

    private String trimAndLimit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }

    private String extractMultipartFilename(HttpServletRequest request) {
        if (!isMultipartRequest(request)) {
            return null;
        }
        try {
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                String submittedFileName = part.getSubmittedFileName();
                if (submittedFileName != null && !submittedFileName.isBlank()) {
                    return sanitizeFileName(submittedFileName);
                }
            }
        } catch (Exception ignored) {
            // ignore multipart parsing failures in audit enrichment
        }
        return null;
    }

    private String sanitizeFileName(String value) {
        String normalized = value.replace("\\", "/");
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < normalized.length() - 1) {
            normalized = normalized.substring(lastSlash + 1);
        }
        return normalized.trim();
    }

    private String firstNonBlank(String... candidates) {
        if (candidates == null) {
            return null;
        }
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return null;
    }

    private String buildResourceLabel(String baseResource, OwnerReference ownerReference) {
        if (!"documents".equals(baseResource) || ownerReference == null) {
            return baseResource;
        }
        return "documents." + ownerReference.ownerType().name().toLowerCase(Locale.ROOT);
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

                Long projectId = extractLong(body, "projectId");
                if (projectId != null) {
                    return projectId;
                }
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
                    Document loaded = document.get();
                    return new OwnerReference(
                            loaded.getOwnerType(),
                            loaded.getOwnerId(),
                            loaded.getOriginalName()
                    );
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
        return new OwnerReference(ownerType, ownerId, null);
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
        return contentType != null && contentType.toLowerCase(Locale.ROOT).contains("application/json");
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");
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
            return DocumentOwnerTypeEnum.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private record OwnerReference(DocumentOwnerTypeEnum ownerType, Long ownerId, String documentName) {
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

        String documentName() {
            return ownerReference != null ? ownerReference.documentName() : null;
        }
    }

    private record AuditActionDescription(
            String action,
            List<ChangedField> changedFields,
            String fileName
    ) {
    }

    private record ChangedField(
            String field,
            String label,
            String value
    ) {
    }
}
