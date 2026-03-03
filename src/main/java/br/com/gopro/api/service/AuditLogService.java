package br.com.gopro.api.service;

import br.com.gopro.api.dtos.AuditLogResponseDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.enums.AuditScopeEnum;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

public interface AuditLogService {

    void log(
            Long actorUserId,
            String action,
            String entityType,
            String entityId,
            Object before,
            Object after,
            HttpServletRequest request
    );

    PageResponseDTO<AuditLogResponseDTO> list(
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
    );
}
