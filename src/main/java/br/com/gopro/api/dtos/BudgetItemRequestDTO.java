package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BudgetItemRequestDTO(
        @NotNull(message = "Categoria e obrigatoria") Long categoryId,
        @NotBlank(message = "Descricao e obrigatoria") String description,
        Integer quantity,
        Integer months,
        BigDecimal unitCost,
        @NotNull(message = "Valor planejado e obrigatorio") BigDecimal plannedAmount,
        BigDecimal executedAmount,
        Long goalId,
        Long projectPeopleId,
        Long projectCompanyId,
        String beneficiaryType,
        BigDecimal contractedAmount,
        String notes,
        @Size(max = 255, message = "WEBS deve ter no maximo 255 caracteres") String webs,
        @Size(max = 255, message = "Ordem de servico deve ter no maximo 255 caracteres") String serviceOrder,
        @Size(max = 255, message = "Protocolo deve ter no maximo 255 caracteres") String protocol,
        Long createdBy
) {}
