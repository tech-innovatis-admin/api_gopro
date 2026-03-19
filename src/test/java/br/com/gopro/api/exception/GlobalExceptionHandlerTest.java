package br.com.gopro.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

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
                .containsExactly(new ErrorResponse.FieldError("email", "E-mail e obrigatorio."));
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
                .containsExactly(new ErrorResponse.FieldError("cnpj", "Ja existe um registro com este CNPJ."));
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

    private HttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/partners");
        request.setMethod("POST");
        return request;
    }
}
