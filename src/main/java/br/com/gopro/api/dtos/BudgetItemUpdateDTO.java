package br.com.gopro.api.dtos;

import java.math.BigDecimal;
import jakarta.validation.constraints.Size;

public record BudgetItemUpdateDTO(
        Long categoryId,
        String description,
        Integer quantity,
        Integer months,
        BigDecimal unitCost,
        BigDecimal plannedAmount,
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
        Long updatedBy
) {}
