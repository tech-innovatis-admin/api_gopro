package br.com.gopro.api.exception;

import java.util.Map;

public class FieldValidationException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public FieldValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
