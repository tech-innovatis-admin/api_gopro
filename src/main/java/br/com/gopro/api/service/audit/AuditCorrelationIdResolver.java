package br.com.gopro.api.service.audit;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public final class AuditCorrelationIdResolver {

    private static final String[] HEADER_CANDIDATES = {
            "X-Correlation-Id",
            "X-Request-Id",
            "X-Trace-Id",
            "traceparent"
    };

    private AuditCorrelationIdResolver() {
    }

    public static String resolve(HttpServletRequest request, String preferred) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        if (request != null) {
            for (String header : HEADER_CANDIDATES) {
                String value = request.getHeader(header);
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
        }
        return UUID.randomUUID().toString();
    }
}

