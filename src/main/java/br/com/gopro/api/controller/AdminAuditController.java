package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.AuditLogResponseDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Tag(name = "Contract Audit", description = "Consulta de trilha de auditoria de contratos")
public class AdminAuditController {

    private final AuditLogService auditLogService;

    @Operation(summary = "Listar logs de auditoria administrativos")
    @GetMapping("/admin/audit")
    @PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
    public ResponseEntity<PageResponseDTO<AuditLogResponseDTO>> listAdminAudit(
            @RequestParam(required = false) AuditScopeEnum scope,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) String actorName,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                auditLogService.list(
                        action,
                        entityType,
                        scope,
                        actorUserId,
                        actorName,
                        search,
                        contractId,
                        from,
                        to,
                        page,
                        size
                )
        );
    }

    @Operation(summary = "Listar logs de auditoria vinculados a contrato")
    @GetMapping("/audit-log")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponseDTO<AuditLogResponseDTO>> listContractAudit(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) String actorName,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                auditLogService.list(
                        action,
                        entityType,
                        AuditScopeEnum.CONTRACTS,
                        actorUserId,
                        actorName,
                        search,
                        contractId,
                        from,
                        to,
                        page,
                        size
                )
        );
    }
}
