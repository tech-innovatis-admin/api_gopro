package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.DocumentOwnerTypeEnum;
import br.com.gopro.api.enums.DocumentStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponseDTO(
        UUID id,
        DocumentOwnerTypeEnum ownerType,
        Long ownerId,
        String category,
        String originalName,
        String contentType,
        Long sizeBytes,
        String sha256,
        String bucket,
        String s3Key,
        DocumentStatusEnum status,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        LocalDateTime deletedAt
) {}
