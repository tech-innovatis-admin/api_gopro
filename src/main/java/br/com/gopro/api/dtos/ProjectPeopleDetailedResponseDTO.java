package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ContractTypeEnum;
import br.com.gopro.api.enums.RoleProjectPeopleEnum;
import br.com.gopro.api.enums.StatusProjectPeopleEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectPeopleDetailedResponseDTO(
        Long id,
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
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy,
        String personFullName,
        String personCpf,
        String personEmail,
        String personPhone,
        String personAvatarUrl,
        LocalDate personBirthDate,
        String personAddress,
        String personZipCode,
        String personCity,
        String personState
) {
}

