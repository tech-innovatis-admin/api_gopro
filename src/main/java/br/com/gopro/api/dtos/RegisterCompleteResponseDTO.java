package br.com.gopro.api.dtos;

public record RegisterCompleteResponseDTO(
        String message,
        AuthLoginResponseDTO auth
) {
}
