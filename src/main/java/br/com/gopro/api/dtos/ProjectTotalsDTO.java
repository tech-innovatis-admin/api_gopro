package br.com.gopro.api.dtos;

import java.math.BigDecimal;

public record ProjectTotalsDTO(
        Long projectId,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal saldo
) {
}
