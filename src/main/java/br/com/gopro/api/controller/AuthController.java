package br.com.gopro.api.controller;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.config.SecurityPrincipalUtils;
import br.com.gopro.api.dtos.AuthLoginRequestDTO;
import br.com.gopro.api.dtos.AuthLoginResponseDTO;
import br.com.gopro.api.dtos.AuthUserResponseDTO;
import br.com.gopro.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticacao e sessao")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login com e-mail ou username")
    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponseDTO> login(
            @Valid @RequestBody AuthLoginRequestDTO dto,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(authService.login(dto, request));
    }

    @Operation(summary = "Dados do usuario autenticado")
    @GetMapping("/me")
    public ResponseEntity<AuthUserResponseDTO> me(Authentication authentication) {
        AuthenticatedUserPrincipal principal = SecurityPrincipalUtils.require(authentication);
        return ResponseEntity.ok(authService.me(principal));
    }
}
