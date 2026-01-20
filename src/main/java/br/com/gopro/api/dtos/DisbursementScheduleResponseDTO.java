package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.StatusDisbursementScheduleEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DisbursementScheduleResponseDTO(
        Long id,
        Long project,
        LocalDate expectedMonth,
        BigDecimal expectedAmount,
        StatusDisbursementScheduleEnum statusDisbursementSchedule,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {
}
