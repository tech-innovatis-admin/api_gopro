package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.RoleProjectPeopleEnum;
import br.com.gopro.api.enums.StatusProjectPeopleEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectPeopleResponseDTO(
        Long id,
        Long project,
        Long People,
        RoleProjectPeopleEnum roleProjectPeople,
        LocalDate startDate,
        LocalDate endDate,
        StatusProjectPeopleEnum statusProjectPeople,
        BigDecimal baseAmount,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}
