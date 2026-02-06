package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.BudgetTransferStatusEnum;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BudgetTransferRequestDTO(
        @NotNull(message = "Projeto e obrigatorio") Long projectId,
        @NotNull(message = "Item de origem e obrigatorio") Long fromItemId,
        @NotNull(message = "Item de destino e obrigatorio") Long toItemId,
        @NotNull(message = "Valor e obrigatorio") BigDecimal amount,
        @NotNull(message = "Data da transferencia e obrigatoria") LocalDate transferDate,
        BudgetTransferStatusEnum status,
        String reason,
        UUID documentId,
        Long createdBy
) {}