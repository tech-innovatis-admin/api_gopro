package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ContractTypeEnum;
import br.com.gopro.api.enums.RoleProjectPeopleEnum;
import br.com.gopro.api.enums.StatusProjectPeopleEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectPeopleUpdateDTO(
        Long projectId,
        Long personId,
        RoleProjectPeopleEnum role,
        BigDecimal workloadHours,
        String institutionalLink,
        ContractTypeEnum contractType,
        LocalDate startDate,
        LocalDate endDate,
        StatusProjectPeopleEnum status,
        BigDecimal baseAmount,
        String notes,
        Long updatedBy
) {}