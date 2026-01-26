package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.BudgetTransferStatusEnum;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetTransferDTO(
        @NotNull(message = "ID do projeto é obrigatório") Long project,
        @NotNull(message = "Item do orçamento é obrigatório") Long budgetItems,
        @NotNull(message = "Rubrica inicial é obrigatório") Long fromBudgetCategories,
        @NotNull(message = "Rúbrica de destino é obrigatório") Long toBudgetCategories,
        @NotNull(message = "Valor total é obrigatório") BigDecimal amount,
        LocalDate transferDate,
        BudgetTransferStatusEnum budgetTransferStatus,
        String reason,
        DocumentRequestDTO document
) {
}
