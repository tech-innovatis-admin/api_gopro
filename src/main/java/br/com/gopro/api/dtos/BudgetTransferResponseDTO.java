package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.BudgetTransferStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetTransferResponseDTO(
        Long id,
        Long projectId,
        Long fromItemId,
        Long toItemId,
        BigDecimal amount,
        LocalDate transferDate,
        BudgetTransferStatusEnum status,
        String reason,
        UUID documentId,
        LocalDateTime approvedAt,
        Long approvedBy,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}