package br.com.gopro.api.dtos;

public record AuthLoginResponseDTO(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        AuthUserResponseDTO user
) {
}
