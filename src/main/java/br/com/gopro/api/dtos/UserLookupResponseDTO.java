package br.com.gopro.api.dtos;

public record UserLookupResponseDTO(
        Long id,
        String fullName,
        String email,
        String username
) {
}

