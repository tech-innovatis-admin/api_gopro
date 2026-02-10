package br.com.gopro.api.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectMonthRequestDTO {

    @NotNull(message = "Mes e obrigatorio")
    @Min(value = 1, message = "Mes deve estar entre 1 e 12")
    @Max(value = 12, message = "Mes deve estar entre 1 e 12")
    private Integer month;

    @Min(value = 2000, message = "Ano deve estar entre 2000 e 2100")
    @Max(value = 2100, message = "Ano deve estar entre 2000 e 2100")
    private Integer year;
}
