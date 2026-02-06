package br.com.gopro.api.dtos;

import java.time.LocalDateTime;

public record DocumentDownloadUrlDTO(
        String url,
        LocalDateTime expiresAt
) {}
