package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.RoleProjectPeopleEnum;
import br.com.gopro.api.enums.StatusProjectPeopleEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectPeopleRequestDTO(
        @NotNull(message = "ID do projeto é obrigatório")
        Long project,

        @NotNull(message = "ID da pessoa é obrigatório")
        Long People,

        @NotBlank(message = "Cargo da pessoa é obrigatório")
        RoleProjectPeopleEnum roleProjectPeople,

        LocalDate starteDate,

        @NotBlank(message = "Status do projeto é obrigatório")
        StatusProjectPeopleEnum statusProjectPeople,

        @NotNull(message = "Preço base é obrigatório")
        BigDecimal baseAmount,
        String notes,
        Long createdBy
) {
}
