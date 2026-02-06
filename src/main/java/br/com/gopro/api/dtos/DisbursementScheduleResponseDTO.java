package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.StatusDisbursementScheduleEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DisbursementScheduleResponseDTO(
        Long id,
        Long projectId,
        Integer numero,
        LocalDate expectedMonth,
        BigDecimal expectedAmount,
        StatusDisbursementScheduleEnum status,
        String notes,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}