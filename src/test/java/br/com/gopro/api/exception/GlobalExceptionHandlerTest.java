package br.com.gopro.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDataIntegrityViolation_shouldExposeRequiredField_whenNotNullConstraintFails() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "insert failed",
                new RuntimeException(
                        "ERROR: null value in column \"email\" of relation \"partners\" violates not-null constraint"
                )
        );

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(exception, request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("E-mail e obrigatorio.");
        assertThat(response.getBody().fieldErrors())
                .containsEntry("email", "E-mail e obrigatorio.");
    }

    @Test
    void handleDataIntegrityViolation_shouldExposeFriendlyMessage_whenUniqueConstraintFails() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "insert failed",
                new RuntimeException(
                        """
                        ERROR: duplicate key value violates unique constraint "partners_cnpj_key"
                          Detail: Key (cnpj)=(10735145000194) already exists.
                        """
                )
        );

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(exception, request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Ja existe um registro com este CNPJ.");
        assertThat(response.getBody().fieldErrors())
                .containsEntry("cnpj", "Ja existe um registro com este CNPJ.");
    }

    @Test
    void handleDataIntegrityViolation_shouldExplainTextLimit_whenValueTooLong() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "insert failed",
                new RuntimeException("ERROR: value too long for type character varying(255)")
        );

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(exception, request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("Um dos campos de texto excedeu o limite de 255 caracteres");
        assertThat(response.getBody().fieldErrors()).isNull();
    }

    @Test
    void handleHttpMessageNotReadable_shouldExposeFriendlyMessage_forInvalidLocalDate() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                """
                JSON parse error: Cannot deserialize value of type `java.time.LocalDate` from String "202-04-03"
                at [Source: UNKNOWN; line: 1, column: 44] (through reference chain: br.com.gopro.api.dtos.DisbursementScheduleRequestDTO["expectedMonth"])
                """,
                new java.time.format.DateTimeParseException("Text '202-04-03' could not be parsed", "202-04-03", 0),
                new MockHttpInputMessage("{}".getBytes(StandardCharsets.UTF_8))
        );

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(exception, request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Mes previsto esta em formato invalido. Use o padrao YYYY-MM-DD.");
        assertThat(response.getBody().fieldErrors())
                .containsEntry("expectedMonth", "Mes previsto esta em formato invalido. Use o padrao YYYY-MM-DD.");
    }

    @Test
    void handleUnauthorized_shouldReturnGenericAuthenticationMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(
                new UnauthorizedException("token expired"),
                request()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Nao foi possivel autenticar sua sessao.");
        assertThat(response.getBody().fieldErrors()).isNull();
    }

    @Test
    void handleAccessDenied_shouldReturnStandardPermissionMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(
                new AccessDeniedException("forbidden"),
                request()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Voce nao tem permissao para executar esta acao.");
        assertThat(response.getBody().fieldErrors()).isNull();
    }

    @Test
    void handleNotFound_shouldReturnSanitizedMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new ResourceNotFoundException("Contrato nao encontrado."),
                request()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Contrato nao encontrado.");
        assertThat(response.getBody().fieldErrors()).isNull();
    }

    private HttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/partners");
        request.setMethod("POST");
        return request;
    }
}
