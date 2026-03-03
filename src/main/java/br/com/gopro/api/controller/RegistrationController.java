package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.AllowedRegistrationValidationResponseDTO;
import br.com.gopro.api.dtos.RegisterCompleteRequestDTO;
import br.com.gopro.api.dtos.RegisterCompleteResponseDTO;
import br.com.gopro.api.service.AllowedRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
@Tag(name = "Registration", description = "Cadastro publico via convite")
public class RegistrationController {

    private final AllowedRegistrationService allowedRegistrationService;

    @Operation(summary = "Validar token de convite")
    @GetMapping("/validate")
    public ResponseEntity<AllowedRegistrationValidationResponseDTO> validate(@RequestParam String token, HttpServletRequest request) {
        return ResponseEntity.ok(allowedRegistrationService.validateInviteToken(token, request));
    }

    @Operation(summary = "Concluir cadastro via token de convite")
    @PostMapping("/complete")
    public ResponseEntity<RegisterCompleteResponseDTO> complete(
            @Valid @RequestBody RegisterCompleteRequestDTO dto,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(allowedRegistrationService.completeRegistration(dto, request));
    }
}
