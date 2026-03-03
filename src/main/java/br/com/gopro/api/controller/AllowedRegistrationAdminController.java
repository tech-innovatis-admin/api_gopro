package br.com.gopro.api.controller;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.config.SecurityPrincipalUtils;
import br.com.gopro.api.dtos.*;
import br.com.gopro.api.enums.AllowedRegistrationStatusEnum;
import br.com.gopro.api.service.AllowedRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/allowed-registrations")
@RequiredArgsConstructor
@Tag(name = "Allowed Registrations", description = "Whitelist de cadastro via convite")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AllowedRegistrationAdminController {

    private final AllowedRegistrationService allowedRegistrationService;

    @Operation(summary = "Criar convite de cadastro")
    @PostMapping
    public ResponseEntity<AllowedRegistrationResponseDTO> createInvite(
            @Valid @RequestBody AllowedRegistrationCreateRequestDTO dto,
            Authentication authentication,
            HttpServletRequest request
    ) {
        AuthenticatedUserPrincipal actor = SecurityPrincipalUtils.require(authentication);
        AllowedRegistrationResponseDTO response = allowedRegistrationService.createInvite(dto, actor, request);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Listar convites")
    @GetMapping
    public ResponseEntity<PageResponseDTO<AllowedRegistrationResponseDTO>> listInvites(
            @RequestParam(required = false) AllowedRegistrationStatusEnum status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(allowedRegistrationService.listInvites(status, page, size));
    }

    @Operation(summary = "Cancelar convite")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AllowedRegistrationResponseDTO> cancelInvite(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request
    ) {
        AuthenticatedUserPrincipal actor = SecurityPrincipalUtils.require(authentication);
        return ResponseEntity.ok(allowedRegistrationService.cancelInvite(id, actor, request));
    }

    @Operation(summary = "Reemitir convite (gera novo token)")
    @PostMapping("/{id}/reissue")
    public ResponseEntity<AllowedRegistrationResponseDTO> reissueInvite(
            @PathVariable Long id,
            @RequestBody(required = false) AllowedRegistrationReissueRequestDTO dto,
            Authentication authentication,
            HttpServletRequest request
    ) {
        AuthenticatedUserPrincipal actor = SecurityPrincipalUtils.require(authentication);
        return ResponseEntity.ok(allowedRegistrationService.reissueInvite(id, dto, actor, request));
    }
}
