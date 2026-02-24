package br.com.gopro.api.exception;

import org.apache.catalina.connector.ClientAbortException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request, null));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException exception, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorResponse.FieldError(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, "Erro de validacao nos campos", request, fieldErrors));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException exception, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorResponse.FieldError(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, "Erro de validacao nos filtros", request, fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> fieldErrors = exception.getConstraintViolations()
                .stream()
                .map(violation -> new ErrorResponse.FieldError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();

        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, "Erro de validacao nos filtros", request, fieldErrors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT;
        String message = mapDataIntegrityMessage(exception);

        return ResponseEntity.status(status)
                .body(buildError(status, message, request, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        if (isClientAbort(exception)) {
            log.debug(
                    "client_abort path={} method={} message={}",
                    request.getRequestURI(),
                    request.getMethod(),
                    exception.getMessage()
            );
            return ResponseEntity.noContent().build();
        }

        log.error("unexpected_error path={} method={} message={}", request.getRequestURI(), request.getMethod(), exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", request, null));
    }

    private boolean isClientAbort(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        if (throwable instanceof ClientAbortException) {
            return true;
        }

        String message = throwable.getMessage();
        if (message != null) {
            String normalized = message.toLowerCase();
            if (normalized.contains("broken pipe") || normalized.contains("connection reset by peer")) {
                return true;
            }
        }

        return isClientAbort(throwable.getCause());
    }

    private ErrorResponse buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<ErrorResponse.FieldError> fieldErrors
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors
        );
    }

    private String mapDataIntegrityMessage(DataIntegrityViolationException exception) {
        String details = "";
        if (exception.getMostSpecificCause() != null && exception.getMostSpecificCause().getMessage() != null) {
            details = exception.getMostSpecificCause().getMessage().toLowerCase();
        } else if (exception.getMessage() != null) {
            details = exception.getMessage().toLowerCase();
        }

        if (details.contains("uq_project_people_active_project_person")) {
            return "Pessoa ja esta vinculada a este projeto";
        }
        if (details.contains("fk_project_people_person_id")) {
            return "Pessoa informada nao encontrada";
        }
        if (details.contains("fk_project_people_project_id")) {
            return "Projeto informado nao encontrado";
        }

        return "Violacao de integridade de dados";
    }
}
