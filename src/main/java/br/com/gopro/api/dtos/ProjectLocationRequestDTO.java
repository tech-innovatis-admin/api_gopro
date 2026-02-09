package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectLocationRequestDTO {

    @NotBlank(message = "Localidade e obrigatoria")
    private String location;
}
