package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetItemContractedAmountUpdateDTO(
        @NotNull(message = "Valor contratado e obrigatorio")
        BigDecimal contractedAmount
) {
}

