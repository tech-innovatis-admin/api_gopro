package br.com.gopro.api.service;

import br.com.gopro.api.dtos.AuditLogResponseDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.model.AuditLog;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.repository.AuditLogRepository;
import br.com.gopro.api.service.audit.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.JoinType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;
    private final AuditSensitiveDataMasker sensitiveDataMasker;
    private final AuditDeltaCalculator deltaCalculator;
    private final AuditMessageFormatter auditMessageFormatter;

    @Override
    public void log(AuditEventRequest event, HttpServletRequest request) {
        if (event == null) {
            return;
        }

        AuditScopeEnum scope = resolveScope(event);
        String acao = normalizeAction(event.getAcao());
        String correlationId = AuditCorrelationIdResolver.resolve(request, event.getCorrelacaoId());

        JsonNode beforeNode = sensitiveDataMasker.toMaskedNode(event.getAntes());
        JsonNode afterNode = sensitiveDataMasker.toMaskedNode(event.getDepois());

        if ("ATUALIZAR".equals(acao)) {
            if (beforeNode == null || beforeNode.isNull()) {
                beforeNode = objectMapper.createObjectNode();
            }
            if (afterNode == null || afterNode.isNull()) {
                afterNode = objectMapper.createObjectNode();
            }
        }

        List<AuditFieldChange> alteracoes = event.getAlteracoes() != null
                ? event.getAlteracoes()
                : List.of();
        if ("ATUALIZAR".equals(acao) && alteracoes.isEmpty()) {
            alteracoes = deltaCalculator.calculate(beforeNode, afterNode);
        }
        alteracoes = sensitiveDataMasker.maskChanges(alteracoes);

        Map<String, Object> detalhesTecnicos = mergeTechnicalDetails(request, event.getDetalhesTecnicos(), correlationId, acao);
        JsonNode detalhesTecnicosNode = sensitiveDataMasker.toMaskedNode(detalhesTecnicos);

        AuditEventRequest enriched = event.toBuilder()
                .tipoAuditoria(scope)
                .acao(acao)
                .correlacaoId(correlationId)
                .alteracoes(alteracoes)
                .antes(jsonToObject(beforeNode))
                .depois(jsonToObject(afterNode))
                .detalhesTecnicos(jsonToObject(detalhesTecnicosNode))
                .build();

        AuditMessage generated = auditMessageFormatter.format(enriched);
        String resumo = firstNonBlank(event.getResumo(), generated.resumo());
        String descricao = firstNonBlank(event.getDescricao(), generated.descricao());

        AuditLog log = new AuditLog();
        if (event.getActorUserId() != null) {
            appUserRepository.findById(event.getActorUserId()).ifPresent(log::setActorUser);
        }

        log.setAuditId(UUID.randomUUID().toString());
        log.setEventAt(OffsetDateTime.now());
        log.setAction(acao);
        log.setEntityType(resolveEntityType(scope, event.getEntidadePrincipal()));
        log.setEntityId(event.getEntidadeId());
        log.setBeforeJson(toJson(beforeNode));
        log.setAfterJson(toJson(afterNode));
        log.setTipoAuditoria(scope);
        log.setModulo(firstNonBlank(event.getModulo(), defaultModulo(scope)));
        log.setFeature(firstNonBlank(event.getFeature(), defaultFeature(scope)));
        log.setEntidadePrincipal(firstNonBlank(event.getEntidadePrincipal(), defaultEntidade(scope)));
        log.setAba(blankToNull(event.getAba()));
        log.setSubsecao(blankToNull(event.getSubsecao()));
        log.setResumo(resumo);
        log.setDescricao(descricao);
        log.setResultado(resolveResultado(event.getResultado()));
        log.setCorrelacaoId(correlationId);
        log.setAlteracoesJson(toJson(alteracoes));
        log.setDetalhesTecnicosJson(toJson(detalhesTecnicosNode));
        log.setIp(extractClientIp(request));
        log.setUserAgent(extractUserAgent(request));

        auditLogRepository.save(log);
    }

    @Override
    public PageResponseDTO<AuditLogResponseDTO> list(
            String action,
            String entityType,
            AuditScopeEnum scope,
            Long actorUserId,
            String actorName,
            String search,
            Long contractId,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }

        Specification<AuditLog> specification = Specification.where(null);

        if (action != null && !action.isBlank()) {
            String normalized = normalizeAction(action);
            specification = specification.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("action")), normalized.toLowerCase(Locale.ROOT)));
        }

        if (entityType != null && !entityType.isBlank()) {
            specification = specification.and(buildEntityTypeSpecification(entityType));
        }

        if (scope != null) {
            specification = specification.and(scopeSpecification(scope));
        }

        if (actorUserId != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("actorUser").get("id"), actorUserId));
        }

        if (actorName != null && !actorName.isBlank()) {
            String pattern = "%" + actorName.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, cb) -> {
                var actor = root.join("actorUser", JoinType.LEFT);
                return cb.or(
                        cb.like(cb.lower(actor.get("fullName")), pattern),
                        cb.like(cb.lower(actor.get("email")), pattern),
                        cb.like(cb.lower(actor.get("username")), pattern)
                );
            });
        }

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(cb.coalesce(root.get("resumo"), "")), pattern),
                    cb.like(cb.lower(cb.coalesce(root.get("descricao"), "")), pattern),
                    cb.like(cb.lower(cb.coalesce(root.get("entityId"), "")), pattern)
            ));
        }

        if (contractId != null) {
            specification = specification.and((root, query, cb) -> cb.and(
                    cb.or(
                            cb.equal(root.get("tipoAuditoria"), AuditScopeEnum.CONTRACTS),
                            cb.like(cb.lower(root.get("entityType")), "contracts:%")
                    ),
                    cb.equal(root.get("entityId"), String.valueOf(contractId))
            ));
        }

        if (from != null) {
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.greaterThanOrEqualTo(root.get("createdAt"), from),
                    cb.greaterThanOrEqualTo(root.get("eventAt"), from.atOffset(OffsetDateTime.now().getOffset()))
            ));
        }

        if (to != null) {
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.lessThanOrEqualTo(root.get("createdAt"), to),
                    cb.lessThanOrEqualTo(root.get("eventAt"), to.atOffset(OffsetDateTime.now().getOffset()))
            ));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventAt").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<AuditLog> logs = auditLogRepository.findAll(specification, pageable);

        List<AuditLogResponseDTO> content = logs.getContent().stream()
                .map(this::toDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                logs.getNumber(),
                logs.getSize(),
                logs.getTotalElements(),
                logs.getTotalPages(),
                logs.isFirst(),
                logs.isLast()
        );
    }

    private Specification<AuditLog> scopeSpecification(AuditScopeEnum scope) {
        return (root, query, cb) -> {
            var entityTypeExpr = cb.lower(root.get("entityType"));
            return switch (scope) {
                case CONTRACTS -> cb.or(
                        cb.equal(root.get("tipoAuditoria"), AuditScopeEnum.CONTRACTS),
                        cb.like(entityTypeExpr, "contracts:%")
                );
                case USERS -> cb.or(
                        cb.equal(root.get("tipoAuditoria"), AuditScopeEnum.USERS),
                        cb.like(entityTypeExpr, "users:%")
                );
                case PEOPLE_COMPANIES -> cb.or(
                        cb.equal(root.get("tipoAuditoria"), AuditScopeEnum.PEOPLE_COMPANIES),
                        cb.like(entityTypeExpr, "people_companies:%")
                );
                case SYSTEM -> cb.or(
                        cb.equal(root.get("tipoAuditoria"), AuditScopeEnum.SYSTEM),
                        cb.like(entityTypeExpr, "system:%")
                );
            };
        };
    }

    private Specification<AuditLog> buildEntityTypeSpecification(String rawEntityType) {
        String normalized = rawEntityType.trim().toLowerCase(Locale.ROOT);
        String pattern = "%" + normalized + "%";

        return (root, query, cb) -> {
            var entityTypeExpr = cb.lower(root.get("entityType"));
            var featureExpr = cb.lower(cb.coalesce(root.get("feature"), ""));
            var subsecaoExpr = cb.lower(cb.coalesce(root.get("subsecao"), ""));

            if (normalized.contains(":")) {
                return cb.equal(entityTypeExpr, normalized);
            }

            return switch (normalized) {
                case "rubrica", "rubricas", "budget", "budgets", "budget-category", "budget-categories", "budget-item", "budget-items", "budget-transfer", "budget-transfers" ->
                        cb.or(
                                cb.like(featureExpr, "%rubrica%"),
                                cb.like(subsecaoExpr, "%rubrica%"),
                                cb.like(subsecaoExpr, "%remanej%")
                        );
                case "income", "incomes", "receita", "receitas" ->
                        cb.or(
                                cb.like(featureExpr, "%receita%")
                        );
                case "expense", "expenses", "despesa", "despesas" ->
                        cb.or(
                                cb.like(featureExpr, "%despesa%")
                        );
                default -> cb.or(
                        cb.equal(entityTypeExpr, normalized),
                        cb.like(featureExpr, pattern),
                        cb.like(subsecaoExpr, pattern)
                );
            };
        };
    }

    private AuditLogResponseDTO toDTO(AuditLog log) {
        AppUser usuario = log.getActorUser();
        return new AuditLogResponseDTO(
                log.getId(),
                log.getAuditId(),
                log.getEventAt(),
                log.getTipoAuditoria(),
                log.getModulo(),
                log.getFeature(),
                log.getEntidadePrincipal(),
                log.getAba(),
                log.getSubsecao(),
                log.getResumo(),
                log.getDescricao(),
                log.getResultado(),
                log.getCorrelacaoId(),
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getFullName() : null,
                usuario != null ? usuario.getEmail() : null,
                usuario != null && usuario.getRole() != null ? usuario.getRole().name() : null,
                log.getAlteracoesJson(),
                log.getDetalhesTecnicosJson(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getBeforeJson(),
                log.getAfterJson(),
                log.getIp(),
                log.getUserAgent(),
                log.getCreatedAt()
        );
    }

    private AuditScopeEnum resolveScope(AuditEventRequest event) {
        if (event.getTipoAuditoria() != null) {
            return event.getTipoAuditoria();
        }
        String entidade = safeLower(event.getEntidadePrincipal());
        if (entidade.contains("contrato") || entidade.contains("project")) {
            return AuditScopeEnum.CONTRACTS;
        }
        if (entidade.contains("usuario") || entidade.contains("user") || entidade.contains("allowed_registration")) {
            return AuditScopeEnum.USERS;
        }
        return AuditScopeEnum.SYSTEM;
    }

    private String normalizeAction(String action) {
        if (action == null || action.isBlank()) {
            return "ATUALIZAR";
        }
        String normalized = action.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "POST", "CREATE", "CRIAR", "INVITE_CREATED", "REGISTER_COMPLETED", "SUPERADMIN_BOOTSTRAPPED" -> "CRIAR";
            case "PUT", "PATCH", "UPDATE", "ATUALIZAR", "USER_UPDATED", "INVITE_REISSUED", "INVITE_CANCELLED", "INVITE_VALIDATED" -> "ATUALIZAR";
            case "DELETE", "EXCLUIR" -> "EXCLUIR";
            case "LOGIN", "LOGIN_SUCCESS", "LOGIN_FAILED" -> "LOGIN";
            case "LOGOUT" -> "LOGOUT";
            case "ERRO", "ERROR" -> "ERRO";
            default -> normalized;
        };
    }

    private String resolveEntityType(AuditScopeEnum scope, String entidadePrincipal) {
        String entidade = blankToNull(entidadePrincipal);
        if (entidade == null) {
            entidade = defaultEntidade(scope);
        }
        String normalized = entidade.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        return scope.prefix() + ":" + normalized;
    }

    private String defaultModulo(AuditScopeEnum scope) {
        return switch (scope) {
            case CONTRACTS -> "Contratos";
            case USERS -> "Usuarios";
            case PEOPLE_COMPANIES -> "Pessoas e Empresas";
            case SYSTEM -> "Sistema";
        };
    }

    private String defaultFeature(AuditScopeEnum scope) {
        return switch (scope) {
            case CONTRACTS -> "Gestao de contratos";
            case USERS -> "Gestao de usuarios";
            case PEOPLE_COMPANIES -> "Gestao de pessoas e empresas";
            case SYSTEM -> "Operacoes do sistema";
        };
    }

    private String defaultEntidade(AuditScopeEnum scope) {
        return switch (scope) {
            case CONTRACTS -> "Contrato";
            case USERS -> "Usuario";
            case PEOPLE_COMPANIES -> "Pessoa/Empresa";
            case SYSTEM -> "Sistema";
        };
    }

    private String resolveResultado(AuditResultEnum result) {
        return result == null ? AuditResultEnum.SUCESSO.name() : result.name();
    }

    private Map<String, Object> mergeTechnicalDetails(
            HttpServletRequest request,
            Object details,
            String correlationId,
            String action
    ) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (details instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                merged.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        if (request != null) {
            merged.putIfAbsent("caminho", request.getRequestURI());
            merged.putIfAbsent("metodoHttp", request.getMethod());
            merged.putIfAbsent("ip", extractClientIp(request));
            merged.putIfAbsent("userAgent", extractUserAgent(request));
        }
        merged.putIfAbsent("correlacaoId", correlationId);
        merged.putIfAbsent("acaoCodigo", action);
        return merged;
    }

    private Object jsonToObject(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return objectMapper.convertValue(node, Object.class);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof String text) {
                String trimmed = text.trim();
                if (trimmed.isEmpty()) {
                    return objectMapper.writeValueAsString(text);
                }
                try {
                    objectMapper.readTree(trimmed);
                    return trimmed;
                } catch (JsonProcessingException ignored) {
                    return objectMapper.writeValueAsString(text);
                }
            }
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{\"serializationError\":true}";
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        return userAgent;
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String safeLower(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT);
    }
}
