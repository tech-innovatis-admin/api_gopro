package br.com.gopro.api.dtos;

import br.com.gopro.api.model.Document;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseRequestDTO(
        @NotNull(message = "Projeto é obrigatório")
        @Schema(description = "ID do projeto")
        Long project,
        @NotNull(message = "Itens do orçamento é obrigatório")
        Long budgetItems,
        @NotNull(message = "Categoria do Orçamento é obrigatório") Long budgetCategories,
        @NotNull(message = "Income é obrigatório") Long income,
        @NotNull(message = "Data da despesa é obrigatório")
        @Schema(example = "2025-01-10", type = "string", format = "date")
        LocalDate expenseDate,
        Integer quantity,
        @NotNull(message = "Total é obrigatório") BigDecimal amount,
        Long projectPeople,
        Long projectOrganization,
        String description,
        String invoiceNumber,
        LocalDate invoiceDate,
        List<DocumentRequestDTO> documents,
        Long createdBy
) {}
