package br.com.gopro.api.dtos;

import br.com.gopro.api.model.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ExpenseResponseDTO(
        Long id,
        Long project,
        Long budgetItems,
        Long budgetCategories,
        Long income,
        LocalDate expenseDate,
        Integer quantity,
        BigDecimal amount,
        Long projectPeople,
        Long projectOrganization,
        String description,
        String invoiceNumber,
        LocalDate invoiceDate,
        List<DocumentResponseDTO> documents,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {
}
