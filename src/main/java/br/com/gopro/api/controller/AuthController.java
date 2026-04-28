package br.com.gopro.api.controller;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.config.SecurityPrincipalUtils;
import br.com.gopro.api.dtos.AuditLogResponseDTO;
import br.com.gopro.api.dtos.AuthForgotPasswordRequestDTO;
import br.com.gopro.api.dtos.AuthLoginRequestDTO;
import br.com.gopro.api.dtos.AuthLoginResponseDTO;
import br.com.gopro.api.dtos.AuthNotificationsReadResponseDTO;
import br.com.gopro.api.dtos.AuthNotificationResponseDTO;
import br.com.gopro.api.dtos.AuthResetPasswordRequestDTO;
import br.com.gopro.api.dtos.AuthUserResponseDTO;
import br.com.gopro.api.dtos.MessageResponseDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.service.AuditLogService;
import br.com.gopro.api.service.AuthService;
import br.com.gopro.api.service.AuthNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticacao e sessao")
public class AuthController {

    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final AuthNotificationService authNotificationService;

    @Operation(summary = "Login com e-mail ou username")
    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponseDTO> login(
            @Valid @RequestBody AuthLoginRequestDTO dto,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(authService.login(dto, request));
    }

    @Operation(summary = "Solicitar reset de senha")
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDTO> forgotPassword(
            @Valid @RequestBody AuthForgotPasswordRequestDTO dto,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(authService.forgotPassword(dto, request));
    }

    @Operation(summary = "Redefinir senha com token")
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(
            @Valid @RequestBody AuthResetPasswordRequestDTO dto,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(authService.resetPassword(dto, request));
    }

    @Operation(summary = "Dados do usuario autenticado")
    @GetMapping("/me")
    public ResponseEntity<AuthUserResponseDTO> me(Authentication authentication) {
        AuthenticatedUserPrincipal principal = SecurityPrincipalUtils.require(authentication);
        return ResponseEntity.ok(authService.me(principal));
    }

    @Operation(summary = "Atualizar foto de perfil do usuario autenticado")
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthUserResponseDTO> updateMyAvatar(
            Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) {
        AuthenticatedUserPrincipal principal = SecurityPrincipalUtils.require(authentication);
        return ResponseEntity.ok(authService.updateMyAvatar(principal, file));
    }

    @Operation(summary = "Listar ultimas acoes do usuario autenticado")
    @GetMapping("/me/audit")
    public ResponseEntity<PageResponseDTO<AuditLogResponseDTO>> meAudit(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        AuthenticatedUserPrincipal principal = SecurityPrincipalUtils.require(authentication);
        return ResponseEntity.ok(
                auditLogService.list(
                        null,
                        null,
                        null,
                        principal.id(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        page,
                        size
                )
        );
    }

    @Operation(summary = "Listar notificacoes do usuario autenticado")
    @GetMapping("/me/notifications")
    public ResponseEntity<List<AuthNotificationResponseDTO>> myNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int size
    ) {
        AuthenticatedUserPrincipal principal = SecurityPrincipalUtils.require(authentication);
        return ResponseEntity.ok(authNotificationService.listNotifications(principal, size));
    }

    @Operation(summary = "Marcar todas as notificacoes como lidas para o usuario autenticado")
    @PostMapping("/me/notifications/read-all")
    public ResponseEntity<AuthNotificationsReadResponseDTO> markAllNotificationsAsRead(
            Authentication authentication
    ) {
        AuthenticatedUserPrincipal principal = SecurityPrincipalUtils.require(authentication);
        return ResponseEntity.ok(authNotificationService.markAllAsRead(principal));
    }
}
