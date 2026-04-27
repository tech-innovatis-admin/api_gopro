package br.com.gopro.api.dtos;

public record EmailDispatchResponseDTO(
        boolean success,
        int statusCode,
        String message,
        String responseBody,
        String responseHeaders
) {
}
