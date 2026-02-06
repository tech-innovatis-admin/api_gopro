package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ContractTypeEnum;
import br.com.gopro.api.enums.RoleProjectPeopleEnum;
import br.com.gopro.api.enums.StatusProjectPeopleEnum;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectPeopleRequestDTO(
        @NotNull(message = "Projeto e obrigatorio") Long projectId,
        @NotNull(message = "Pessoa e obrigatoria") Long personId,
        RoleProjectPeopleEnum role,
        BigDecimal workloadHours,
        String institutionalLink,
        ContractTypeEnum contractType,
        LocalDate startDate,
        LocalDate endDate,
        StatusProjectPeopleEnum status,
        BigDecimal baseAmount,
        String notes,
        Long createdBy
) {}