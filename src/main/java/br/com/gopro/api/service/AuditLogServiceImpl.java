package br.com.gopro.api.service;

import br.com.gopro.api.dtos.AuditLogResponseDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.model.AuditLog;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void log(
            Long actorUserId,
            String action,
            String entityType,
            String entityId,
            Object before,
            Object after,
            HttpServletRequest request
    ) {
        AuditLog log = new AuditLog();
        if (actorUserId != null) {
            appUserRepository.findById(actorUserId).ifPresent(log::setActorUser);
        }
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setBeforeJson(toJson(before));
        log.setAfterJson(toJson(after));
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
            specification = specification.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("action")), action.trim().toLowerCase()));
        }

        if (entityType != null && !entityType.isBlank()) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("entityType")), entityType.trim().toLowerCase()));
        }

        if (scope != null) {
            specification = specification.and(scopeSpecification(scope));
        }

        if (actorUserId != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("actorUser").get("id"), actorUserId));
        }

        if (actorName != null && !actorName.isBlank()) {
            String pattern = "%" + actorName.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("actorUser").get("fullName")), pattern),
                    cb.like(cb.lower(root.get("actorUser").get("email")), pattern),
                    cb.like(cb.lower(root.get("actorUser").get("username")), pattern)
            ));
        }

        if (contractId != null) {
            specification = specification.and((root, query, cb) -> cb.and(
                    cb.like(cb.lower(root.get("entityType")), "contracts:%"),
                    cb.equal(root.get("entityId"), String.valueOf(contractId))
            ));
        }

        if (from != null) {
            specification = specification.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }

        if (to != null) {
            specification = specification.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
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
            var entityType = cb.lower(root.get("entityType"));

            return switch (scope) {
                case CONTRACTS -> cb.like(entityType, "contracts:%");
                case PEOPLE_COMPANIES -> cb.like(entityType, "people_companies:%");
                case SYSTEM -> cb.or(
                        cb.like(entityType, "system:%"),
                        cb.and(
                                cb.notLike(entityType, "contracts:%"),
                                cb.notLike(entityType, "people_companies:%")
                        )
                );
            };
        };
    }

    private AuditLogResponseDTO toDTO(AuditLog log) {
        AppUser actor = log.getActorUser();
        return new AuditLogResponseDTO(
                log.getId(),
                actor != null ? actor.getId() : null,
                actor != null ? actor.getEmail() : null,
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
}
