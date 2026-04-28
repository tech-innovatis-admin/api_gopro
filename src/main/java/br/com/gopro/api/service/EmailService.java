package br.com.gopro.api.service;

import br.com.gopro.api.enums.UserRoleEnum;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    @Value("${sendgrid.enabled:false}")
    private boolean enabled;

    @Value("${sendgrid.template.project-deadline-id:}")
    private String projectDeadlineTemplateId;

    @Value("${sendgrid.template.invite-id:}")
    private String inviteTemplateId;

    @Value("${sendgrid.template.password-reset-id:}")
    private String passwordResetTemplateId;

    @Value("${app.frontend.contract-base-url:http://localhost:3000/contratos}")
    private String contractBaseUrl;

    public EmailDispatchResult sendProjectDeadlineNotification(
            String email,
            String recipientName,
            br.com.gopro.api.model.Project project,
            long daysRemaining
    ) {
        if (!enabled) {
            return EmailDispatchResult.disabled("Envio de email desabilitado por configuração");
        }
        if (project == null || project.getId() == null || project.getEndDate() == null) {
            return EmailDispatchResult.invalid("Projeto inválido para envio de email de prazo");
        }
        if (apiKey == null || apiKey.isBlank() || fromEmail == null || fromEmail.isBlank()) {
            log.warn("sendgrid não configurado completamente; envio de email de prazo ignorado");
            return EmailDispatchResult.invalid("SendGrid não configurado completamente");
        }

        Mail mail = new Mail();
        mail.setFrom(new Email(fromEmail, fromName));

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(email, recipientName));

        if (projectDeadlineTemplateId != null && !projectDeadlineTemplateId.isBlank()) {
            personalization.addDynamicTemplateData("recipientName", recipientName);
            personalization.addDynamicTemplateData("contractLabel", buildContractLabel(project));
            personalization.addDynamicTemplateData("contractCode", project.getCode());
            personalization.addDynamicTemplateData("contractName", project.getName());
            personalization.addDynamicTemplateData("contractEndDate", formatDate(project.getEndDate()));
            personalization.addDynamicTemplateData("daysRemaining", daysRemaining);
            personalization.addDynamicTemplateData("timeRemainingText", buildTimeRemainingText(daysRemaining));
            personalization.addDynamicTemplateData("deadlineMessage", buildDeadlineSentence(project, daysRemaining));
            personalization.addDynamicTemplateData("contractUrl", buildContractUrl(project.getId()));
            mail.addPersonalization(personalization);
            mail.setTemplateId(projectDeadlineTemplateId);
        } else {
            mail.addPersonalization(personalization);
            mail.setSubject(buildSubject(project, daysRemaining));
            mail.addContent(new Content("text/plain", buildPlainTextBody(recipientName, project, daysRemaining)));
        }

        return dispatch(mail, email, "project-deadline");
    }

    public EmailDispatchResult sendPlainTextTestEmail(
            String email,
            String recipientName,
            String subject,
            String message
    ) {
        if (!enabled) {
            return EmailDispatchResult.disabled("Envio de email desabilitado por configuração");
        }
        if (apiKey == null || apiKey.isBlank() || fromEmail == null || fromEmail.isBlank()) {
            return EmailDispatchResult.invalid("SendGrid não configurado completamente");
        }

        Mail mail = new Mail();
        mail.setFrom(new Email(fromEmail, fromName));
        mail.setSubject(subject);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(email, recipientName));
        mail.addPersonalization(personalization);
        mail.addContent(new Content("text/plain", message));

        return dispatch(mail, email, "manual-test");
    }

    public EmailDispatchResult sendInviteEmail(
            String email,
            UserRoleEnum role,
            String inviteLink,
            LocalDateTime expiresAt
    ) {
        if (!enabled) {
            return EmailDispatchResult.disabled("Envio de email desabilitado por configuração");
        }
        if (apiKey == null || apiKey.isBlank() || fromEmail == null || fromEmail.isBlank()) {
            return EmailDispatchResult.invalid("SendGrid não configurado completamente");
        }
        if (email == null || email.isBlank() || inviteLink == null || inviteLink.isBlank()) {
            return EmailDispatchResult.invalid("Dados inválidos para envio do convite");
        }

        Mail mail = new Mail();
        mail.setFrom(new Email(fromEmail, fromName));

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(email));

        if (inviteTemplateId != null && !inviteTemplateId.isBlank()) {
            personalization.addDynamicTemplateData("inviteeEmail", email);
            personalization.addDynamicTemplateData("roleLabel", buildRoleLabel(role));
            personalization.addDynamicTemplateData("inviteLink", inviteLink);
            personalization.addDynamicTemplateData("expiresAt", formatDateTime(expiresAt));
            mail.addPersonalization(personalization);
            mail.setTemplateId(inviteTemplateId);
        } else {
            mail.addPersonalization(personalization);
            mail.setSubject("Convite para acesso à plataforma GoPro");
            mail.addContent(new Content("text/plain", buildInviteBody(role, inviteLink, expiresAt)));
        }

        return dispatch(mail, email, "invite");
    }

    public EmailDispatchResult sendPasswordResetEmail(
            String email,
            String recipientName,
            String resetLink,
            LocalDateTime expiresAt
    ) {
        if (!enabled) {
            return EmailDispatchResult.disabled("Envio de email desabilitado por configuração");
        }
        if (apiKey == null || apiKey.isBlank() || fromEmail == null || fromEmail.isBlank()) {
            return EmailDispatchResult.invalid("SendGrid não configurado completamente");
        }
        if (email == null || email.isBlank() || resetLink == null || resetLink.isBlank()) {
            return EmailDispatchResult.invalid("Dados inválidos para envio do reset de senha");
        }

        Mail mail = new Mail();
        mail.setFrom(new Email(fromEmail, fromName));

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(email, recipientName));

        if (passwordResetTemplateId != null && !passwordResetTemplateId.isBlank()) {
            personalization.addDynamicTemplateData("recipientName", recipientName);
            personalization.addDynamicTemplateData("resetLink", resetLink);
            personalization.addDynamicTemplateData("expiresAt", formatDateTime(expiresAt));
            mail.addPersonalization(personalization);
            mail.setTemplateId(passwordResetTemplateId);
        } else {
            mail.addPersonalization(personalization);
            mail.setSubject("Redefinição de senha da plataforma GoPro");
            mail.addContent(new Content("text/plain", buildPasswordResetBody(recipientName, resetLink, expiresAt)));
        }

        return dispatch(mail, email, "password-reset");
    }

    private Response send(Mail mail) throws IOException {
        SendGrid sendGrid = new SendGrid(apiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        return sendGrid.api(request);
    }

    private EmailDispatchResult dispatch(Mail mail, String email, String operation) {
        try {
            Response response = send(mail);
            boolean success = response.getStatusCode() >= 200 && response.getStatusCode() < 300;
            if (!success) {
                log.warn(
                        "falha ao enviar email operation={} to={} status={} body={}",
                        operation,
                        email,
                        response.getStatusCode(),
                        safeBody(response)
                );
            }
            return new EmailDispatchResult(
                    success,
                    response.getStatusCode(),
                    response.getHeaders() == null ? null : response.getHeaders().toString(),
                    safeBody(response),
                    success ? "Email enviado com sucesso" : "SendGrid retornou erro no envio"
            );
        } catch (IOException ex) {
            log.error("erro ao enviar email operation={} to={}", operation, email, ex);
            return EmailDispatchResult.error("Erro ao enviar email: " + ex.getMessage());
        }
    }

    private String safeBody(Response response) {
        if (response == null || response.getBody() == null || response.getBody().isBlank()) {
            return null;
        }
        return response.getBody();
    }

    private String buildSubject(br.com.gopro.api.model.Project project, long daysRemaining) {
        return buildDeadlineSentence(project, daysRemaining);
    }

    private String buildPlainTextBody(String recipientName, br.com.gopro.api.model.Project project, long daysRemaining) {
        String greeting = recipientName == null || recipientName.isBlank() ? "Ola" : "Ola, " + recipientName;
        String contractLabel = buildContractLabel(project);
        String projectName = project.getName() == null || project.getName().isBlank() ? "" : " - " + project.getName();

        return greeting + ",\n\n"
                + "Falta(m) " + buildTimeRemainingText(daysRemaining) + " para o vencimento do "
                + contractLabel + projectName + ".\n"
                + "Data final: " + formatDate(project.getEndDate()) + "\n"
                + "Confira: " + buildContractUrl(project.getId()) + "\n";
    }

    private String buildDeadlineSentence(br.com.gopro.api.model.Project project, long daysRemaining) {
        return buildTimeRemainingText(daysRemaining) + " para o vencimento do contrato";
    }

    private String buildTimeRemainingText(long daysRemaining) {
        if (daysRemaining <= 0) {
            return "0 dias";
        }
        if (daysRemaining == 1) {
            return "1 dia";
        }
        return daysRemaining + " dias";
    }

    private String buildContractLabel(br.com.gopro.api.model.Project project) {
        if (project == null) {
            return "contrato";
        }
        if (project.getCode() != null && !project.getCode().isBlank()) {
            return "contrato " + project.getCode();
        }
        if (project.getId() != null) {
            return "contrato #" + project.getId();
        }
        return "contrato";
    }

    private String formatDate(LocalDate date) {
        return date == null ? null : DATE_FORMATTER.format(date);
    }

    private String buildContractUrl(Long contractId) {
        String base = contractBaseUrl == null ? "" : contractBaseUrl.trim();
        if (base.endsWith("/")) {
            return base + contractId;
        }
        return base + "/" + contractId;
    }

    private String buildInviteBody(UserRoleEnum role, String inviteLink, LocalDateTime expiresAt) {
        return "Você recebeu um convite para acessar a plataforma GoPro. Acesse o link abaixo para concluir o seu cadastro.\n\n"
                + "Perfil: " + buildRoleLabel(role) + "\n"
                + "Link de cadastro: " + inviteLink + "\n"
                + "Validade: " + formatDateTime(expiresAt) + "\n";
    }

    private String buildPasswordResetBody(String recipientName, String resetLink, LocalDateTime expiresAt) {
        String greeting = recipientName == null || recipientName.isBlank() ? "Olá" : "Olá, " + recipientName;
        return greeting + ",\n\n"
                + "Recebemos uma solicitação para redefinir sua senha.\n"
                + "Use o link abaixo para continuar:\n"
                + resetLink + "\n\n"
                + "Validade: " + formatDateTime(expiresAt) + "\n"
                + "Se você não solicitou a redefinição, ignore este email.\n";
    }

    private String buildRoleLabel(UserRoleEnum role) {
        if (role == null) {
            return "Usuario";
        }
        return switch (role) {
            case SUPERADMIN -> "Superadmin";
            case ADMIN -> "Admin";
            case OWNER -> "Owner";
            case ANALISTA -> "Analista";
            case ESTAGIARIO -> "Estagiario";
        };
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public record EmailDispatchResult(
            boolean success,
            int statusCode,
            String responseHeaders,
            String responseBody,
            String message
    ) {
        static EmailDispatchResult disabled(String message) {
            return new EmailDispatchResult(false, HttpStatus.PRECONDITION_FAILED.value(), null, null, message);
        }

        static EmailDispatchResult invalid(String message) {
            return new EmailDispatchResult(false, HttpStatus.BAD_REQUEST.value(), null, null, message);
        }

        static EmailDispatchResult error(String message) {
            return new EmailDispatchResult(false, HttpStatus.BAD_GATEWAY.value(), null, null, message);
        }
    }
}
