package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetItemBeneficiaryAssignRequestDTO(
        @NotBlank(message = "Tipo de beneficiario e obrigatorio")
        String beneficiaryType,
        @NotNull(message = "Referencia do beneficiario e obrigatoria")
        Long referenceId,
        @NotNull(message = "Valor contratado e obrigatorio")
        BigDecimal contractedAmount
) {
}

