package br.com.gopro.api.exception;

import java.util.Map;

public record ErrorResponse(
        int status,
        String message,
        Map<String, String> fieldErrors
) {
}
