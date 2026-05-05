package br.com.gopro.api.exception;

import org.apache.catalina.connector.ClientAbortException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String VALIDATION_MESSAGE = "Existem campos invalidos no formulario.";
    private static final String CONFLICT_MESSAGE = "Ja existe um registro com estas informacoes.";
    private static final Pattern NOT_NULL_COLUMN_PATTERN = Pattern.compile(
            "null value in column \"([^\"]+)\"(?: of relation \"([^\"]+)\")? violates not-null constraint",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern UNIQUE_CONSTRAINT_PATTERN = Pattern.compile(
            "duplicate key value violates unique constraint \"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern UNIQUE_KEY_DETAIL_PATTERN = Pattern.compile(
            "key \\(([^)]+)\\)=\\(([^)]*)\\) already exists",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FOREIGN_KEY_CONSTRAINT_PATTERN = Pattern.compile(
            "violates foreign key constraint \"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FOREIGN_KEY_MISSING_DETAIL_PATTERN = Pattern.compile(
            "key \\(([^)]+)\\)=\\(([^)]*)\\) is not present in table",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FOREIGN_KEY_REFERENCED_DETAIL_PATTERN = Pattern.compile(
            "key \\(([^)]+)\\)=\\(([^)]*)\\) is still referenced from table",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern VARCHAR_LENGTH_PATTERN = Pattern.compile(
            "value too long for type character varying\\((\\d+)\\)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern LENGTH_COLUMN_PATTERN = Pattern.compile(
            "column \"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern JSON_REFERENCE_FIELD_PATTERN = Pattern.compile("\\[\"([^\"]+)\"\\]");
    private static final Map<String, String> FIELD_LABELS = Map.ofEntries(
            Map.entry("email", "E-mail"),
            Map.entry("cnpj", "CNPJ"),
            Map.entry("cpf", "CPF"),
            Map.entry("name", "Nome"),
            Map.entry("full_name", "Nome completo"),
            Map.entry("trade_name", "Nome fantasia"),
            Map.entry("acronym", "Sigla"),
            Map.entry("code", "Codigo"),
            Map.entry("phone", "Telefone"),
            Map.entry("address", "Endereco"),
            Map.entry("site", "Site"),
            Map.entry("city", "Cidade"),
            Map.entry("state", "UF"),
            Map.entry("contact_person", "Responsavel de contato"),
            Map.entry("responsible_person_id", "Pessoa responsavel"),
            Map.entry("project_id", "Contrato"),
            Map.entry("partner_id", "Parceiro"),
            Map.entry("primary_partner_id", "Parceiro primario"),
            Map.entry("secundary_partner_id", "Parceiro secundario"),
            Map.entry("primary_client_id", "Cliente primario"),
            Map.entry("secundary_client_id", "Cliente secundario"),
            Map.entry("public_agency_id", "Cliente primario"),
            Map.entry("secretary_id", "Secretaria"),
            Map.entry("cordinator_id", "Coordenador"),
            Map.entry("goal_id", "Meta"),
            Map.entry("stage_id", "Etapa"),
            Map.entry("phase_id", "Fase"),
            Map.entry("expectedmonth", "Mes previsto"),
            Map.entry("category_id", "Rubrica"),
            Map.entry("budget_item_id", "Subitem"),
            Map.entry("income_id", "Recebimento"),
            Map.entry("person_id", "Pessoa"),
            Map.entry("organization_id", "Empresa"),
            Map.entry("document_id", "Documento"),
            Map.entry("numero", "Numero"),
            Map.entry("titulo", "Titulo"),
            Map.entry("descricao", "Descricao"),
            Map.entry("notes", "Observacoes")
    );

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, sanitizeNotFoundMessage(exception.getMessage()), null));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException exception, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, sanitizeBusinessMessage(exception.getMessage()), null));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildError(HttpStatus.UNAUTHORIZED, "Nao foi possivel autenticar sua sessao.", null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildError(HttpStatus.FORBIDDEN, "Voce nao tem permissao para executar esta acao.", null));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(
            TooManyRequestsException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(buildError(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(LinkedHashMap::new, (map, fieldError) -> map.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()), LinkedHashMap::putAll);

        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, VALIDATION_MESSAGE, fieldErrors));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(LinkedHashMap::new, (map, fieldError) -> map.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()), LinkedHashMap::putAll);

        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, VALIDATION_MESSAGE, fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = exception.getConstraintViolations()
                .stream()
                .collect(
                        LinkedHashMap::new,
                        (map, violation) -> map.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()),
                        LinkedHashMap::putAll
                );

        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, VALIDATION_MESSAGE, fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        if (isInvalidDatePayload(exception)) {
            String fieldName = extractJsonReferenceField(exception.getMessage());
            String message = resolveInvalidDateMessage(fieldName);
            Map<String, String> fieldErrors = fieldName == null
                    ? null
                    : Map.of(fieldName, message);

            return ResponseEntity.badRequest()
                    .body(buildError(HttpStatus.BAD_REQUEST, message, fieldErrors));
        }

        return ResponseEntity.badRequest()
                .body(buildError(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido.", null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        ParsedDataIntegrityError parsed = parseDataIntegrityViolation(exception);

        return ResponseEntity.status(parsed.status())
                .body(buildError(parsed.status(), parsed.message(), parsed.fieldErrors()));
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
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", null));
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
            Map<String, String> fieldErrors
    ) {
        return new ErrorResponse(
                status.value(),
                message,
                fieldErrors
        );
    }

    private String sanitizeNotFoundMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Recurso nao encontrado.";
        }
        return message;
    }

    private String sanitizeBusinessMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Nao foi possivel processar a solicitacao.";
        }
        return message;
    }

    private ParsedDataIntegrityError parseDataIntegrityViolation(DataIntegrityViolationException exception) {
        String rawDetails = dataIntegrityDetails(exception);
        String normalizedDetails = rawDetails.toLowerCase(Locale.ROOT);

        ParsedDataIntegrityError mappedConstraint = mapKnownConstraint(normalizedDetails);
        if (mappedConstraint != null) {
            return mappedConstraint;
        }

        ParsedDataIntegrityError notNullViolation = parseNotNullViolation(rawDetails);
        if (notNullViolation != null) {
            return notNullViolation;
        }

        ParsedDataIntegrityError uniqueViolation = parseUniqueViolation(rawDetails);
        if (uniqueViolation != null) {
            return uniqueViolation;
        }

        ParsedDataIntegrityError foreignKeyViolation = parseForeignKeyViolation(rawDetails);
        if (foreignKeyViolation != null) {
            return foreignKeyViolation;
        }

        ParsedDataIntegrityError lengthViolation = parseLengthViolation(rawDetails);
        if (lengthViolation != null) {
            return lengthViolation;
        }

        return new ParsedDataIntegrityError(HttpStatus.CONFLICT, CONFLICT_MESSAGE, null);
    }

    private ParsedDataIntegrityError mapKnownConstraint(String normalizedDetails) {
        if (normalizedDetails.contains("uq_project_people_active_project_person")) {
            return new ParsedDataIntegrityError(
                    HttpStatus.CONFLICT,
                    "Pessoa ja esta vinculada a este projeto",
                    null
            );
        }
        if (normalizedDetails.contains("uq_budget_categories_project_name")) {
            return new ParsedDataIntegrityError(
                    HttpStatus.CONFLICT,
                    "Ja existe uma rubrica com este nome neste contrato.",
                    List.of(
                            new ErrorResponse.FieldError("projectId", "Contrato ja possui uma rubrica com este nome."),
                            new ErrorResponse.FieldError("name", "Ja existe uma rubrica com este nome neste contrato.")
                    )
            );
        }
        if (normalizedDetails.contains("fk_project_people_person_id")) {
            return new ParsedDataIntegrityError(
                    HttpStatus.CONFLICT,
                    "Pessoa informada nao encontrada",
                    Map.of("personId", "Pessoa informada nao encontrada.")
            );
        }
        if (normalizedDetails.contains("fk_project_people_project_id")) {
            return new ParsedDataIntegrityError(
                    HttpStatus.CONFLICT,
                    "Projeto informado nao encontrado",
                    Map.of("projectId", "Contrato informado nao encontrado.")
            );
        }
        return null;
    }

    private ParsedDataIntegrityError parseNotNullViolation(String rawDetails) {
        Matcher matcher = NOT_NULL_COLUMN_PATTERN.matcher(rawDetails);
        if (!matcher.find()) {
            return null;
        }

        String columnName = matcher.group(1);
        String fieldKey = normalizeFieldKey(columnName);
        String label = resolveFieldLabel(columnName);
        Map<String, String> fieldErrors = Map.of(fieldKey, label + " e obrigatorio.");

        return new ParsedDataIntegrityError(
                HttpStatus.CONFLICT,
                summarizeFieldErrors(fieldErrors, "Campo obrigatorio nao informado"),
                fieldErrors
        );
    }

    private ParsedDataIntegrityError parseUniqueViolation(String rawDetails) {
        Matcher matcher = UNIQUE_CONSTRAINT_PATTERN.matcher(rawDetails);
        if (!matcher.find()) {
            return null;
        }

        Matcher keyMatcher = UNIQUE_KEY_DETAIL_PATTERN.matcher(rawDetails);
        if (keyMatcher.find()) {
            Map<String, String> fieldErrors = buildFieldErrors(
                    keyMatcher.group(1),
                    columnName -> "Ja existe um registro com este " + resolveFieldLabelForSentence(columnName) + "."
            );

            return new ParsedDataIntegrityError(
                    HttpStatus.CONFLICT,
                    summarizeFieldErrors(fieldErrors, CONFLICT_MESSAGE),
                    fieldErrors
            );
        }

        return new ParsedDataIntegrityError(
                HttpStatus.CONFLICT,
                CONFLICT_MESSAGE,
                null
        );
    }

    private ParsedDataIntegrityError parseForeignKeyViolation(String rawDetails) {
        Matcher constraintMatcher = FOREIGN_KEY_CONSTRAINT_PATTERN.matcher(rawDetails);
        if (!constraintMatcher.find()) {
            return null;
        }

        Matcher missingMatcher = FOREIGN_KEY_MISSING_DETAIL_PATTERN.matcher(rawDetails);
        if (missingMatcher.find()) {
            Map<String, String> fieldErrors = buildFieldErrors(
                    missingMatcher.group(1),
                    columnName -> "Valor informado para " + resolveFieldLabelForSentence(columnName) + " nao foi encontrado."
            );

            return new ParsedDataIntegrityError(
                    HttpStatus.CONFLICT,
                    summarizeFieldErrors(fieldErrors, "Referencia informada nao encontrada"),
                    fieldErrors
            );
        }

        Matcher referencedMatcher = FOREIGN_KEY_REFERENCED_DETAIL_PATTERN.matcher(rawDetails);
        if (referencedMatcher.find()) {
            return new ParsedDataIntegrityError(
                    HttpStatus.CONFLICT,
                    "Nao e possivel remover este registro porque ele esta vinculado a outros cadastros",
                    null
            );
        }

        return new ParsedDataIntegrityError(
                HttpStatus.CONFLICT,
                "Referencia informada e invalida",
                null
        );
    }

    private ParsedDataIntegrityError parseLengthViolation(String rawDetails) {
        Matcher matcher = VARCHAR_LENGTH_PATTERN.matcher(rawDetails);
        if (!matcher.find()) {
            return null;
        }

        Matcher columnMatcher = LENGTH_COLUMN_PATTERN.matcher(rawDetails);
        if (columnMatcher.find()) {
            String columnName = columnMatcher.group(1);
            String fieldKey = normalizeFieldKey(columnName);
            String label = resolveFieldLabel(columnName);
            Map<String, String> fieldErrors = Map.of(
                    fieldKey,
                    label + " excedeu o limite de " + matcher.group(1) + " caracteres."
            );

            return new ParsedDataIntegrityError(
                    HttpStatus.CONFLICT,
                    summarizeFieldErrors(fieldErrors, "Campo com tamanho invalido."),
                    fieldErrors
            );
        }

        return new ParsedDataIntegrityError(
                HttpStatus.CONFLICT,
                "Um dos campos de texto excedeu o limite de " + matcher.group(1) + " caracteres",
                null
        );
    }

    private boolean isInvalidDatePayload(HttpMessageNotReadableException exception) {
        Throwable rootCause = exception.getMostSpecificCause();
        if (rootCause instanceof DateTimeParseException) {
            return true;
        }

        String message = exception.getMessage();
        return message != null && message.contains("LocalDate");
    }

    private String extractJsonReferenceField(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return null;
        }

        Matcher matcher = JSON_REFERENCE_FIELD_PATTERN.matcher(rawMessage);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }

    private String resolveInvalidDateMessage(String fieldName) {
        String label = fieldName == null ? "Data" : resolveFieldLabel(fieldName);
        return label + " esta em formato invalido. Use o padrao YYYY-MM-DD.";
    }

    private Map<String, String> buildFieldErrors(
            String rawFieldList,
            java.util.function.Function<String, String> messageBuilder
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        List.of(rawFieldList.split(","))
                .stream()
                .map(String::trim)
                .filter(field -> !field.isBlank())
                .forEach(field -> fieldErrors.putIfAbsent(normalizeFieldKey(field), messageBuilder.apply(field)));
        return fieldErrors;
    }

    private String summarizeFieldErrors(Map<String, String> fieldErrors, String fallback) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return fallback;
        }

        return fieldErrors.values().stream()
                .distinct()
                .reduce((left, right) -> left + " | " + right)
                .orElse(fallback);
    }

    private String resolveFieldLabel(String fieldName) {
        String normalized = fieldName == null ? "" : fieldName.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "Campo";
        }

        String mapped = FIELD_LABELS.get(normalized);
        if (mapped != null) {
            return mapped;
        }

        String humanized = normalized
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replace('_', ' ')
                .trim();

        if (humanized.isBlank()) {
            return "Campo";
        }

        return Character.toUpperCase(humanized.charAt(0)) + humanized.substring(1);
    }

    private String resolveFieldLabelForSentence(String fieldName) {
        String label = resolveFieldLabel(fieldName);
        if (label.isBlank()) {
            return "campo";
        }
        if (label.equals(label.toUpperCase(Locale.ROOT))) {
            return label;
        }
        return Character.toLowerCase(label.charAt(0)) + label.substring(1);
    }

    private String normalizeFieldKey(String fieldName) {
        if (fieldName == null) {
            return "general";
        }

        String normalized = fieldName.trim().toLowerCase(Locale.ROOT);
        if (!normalized.contains("_")) {
            return normalized;
        }

        String[] parts = normalized.split("_+");
        if (parts.length == 0) {
            return normalized;
        }

        StringBuilder builder = new StringBuilder(parts[0]);
        for (int index = 1; index < parts.length; index++) {
            if (parts[index].isBlank()) {
                continue;
            }
            builder.append(Character.toUpperCase(parts[index].charAt(0)));
            if (parts[index].length() > 1) {
                builder.append(parts[index].substring(1));
            }
        }
        return builder.toString();
    }

    private String dataIntegrityDetails(DataIntegrityViolationException exception) {
        if (exception.getMostSpecificCause() != null && exception.getMostSpecificCause().getMessage() != null) {
            return exception.getMostSpecificCause().getMessage();
        }
        if (exception.getMessage() != null) {
            return exception.getMessage();
        }
        return "";
    }

    private record ParsedDataIntegrityError(
            HttpStatus status,
            String message,
            Map<String, String> fieldErrors
    ) {
    }
}
