package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.EmailDispatchResponseDTO;
import br.com.gopro.api.dtos.EmailTestRequestDTO;
import br.com.gopro.api.dtos.ProjectDeadlineTriggerRequestDTO;
import br.com.gopro.api.dtos.ProjectDeadlineTriggerResponseDTO;
import br.com.gopro.api.service.EmailService;
import br.com.gopro.api.service.ProjectDeadlineEmailNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/emails")
@RequiredArgsConstructor
@Tag(name = "Admin Emails", description = "Teste e disparo manual de emails")
@PreAuthorize("hasAnyRole('SUPERADMIN','ADMIN')")
public class AdminEmailController {

    private final EmailService emailService;
    private final ProjectDeadlineEmailNotificationService projectDeadlineEmailNotificationService;

    @Operation(summary = "Enviar email de teste")
    @PostMapping("/test")
    public ResponseEntity<EmailDispatchResponseDTO> sendTestEmail(
            @Valid @RequestBody EmailTestRequestDTO dto
    ) {
        EmailService.EmailDispatchResult result = emailService.sendPlainTextTestEmail(
                dto.email(),
                dto.recipientName(),
                dto.subject(),
                dto.message()
        );

        return ResponseEntity.status(result.statusCode())
                .body(new EmailDispatchResponseDTO(
                        result.success(),
                        result.statusCode(),
                        result.message(),
                        result.responseBody(),
                        result.responseHeaders()
                ));
    }

    @Operation(summary = "Disparar manualmente notificacoes de prazo de contrato")
    @PostMapping("/project-deadline/trigger")
    public ResponseEntity<ProjectDeadlineTriggerResponseDTO> triggerProjectDeadlines(
            @Valid @RequestBody ProjectDeadlineTriggerRequestDTO dto
    ) {
        ProjectDeadlineEmailNotificationService.ProjectDeadlineDispatchSummary summary =
                projectDeadlineEmailNotificationService.triggerManual(dto.referenceDate());

        return ResponseEntity.ok(new ProjectDeadlineTriggerResponseDTO(
                summary.referenceDate(),
                summary.projectsEvaluated(),
                summary.recipientsEvaluated(),
                summary.emailsSent(),
                summary.emailsSkipped(),
                summary.notificationsEnabled(),
                summary.message()
        ));
    }
}
