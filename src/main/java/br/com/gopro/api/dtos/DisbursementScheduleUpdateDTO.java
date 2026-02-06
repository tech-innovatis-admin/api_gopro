package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.StatusDisbursementScheduleEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DisbursementScheduleUpdateDTO(
        Long projectId,
        Integer numero,
        LocalDate expectedMonth,
        BigDecimal expectedAmount,
        StatusDisbursementScheduleEnum status,
        String notes,
        Long updatedBy
) {}