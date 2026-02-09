package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectTypeDistributionRequestDTO {

    @NotNull(message = "Tipo do projeto e obrigatorio")
    private ProjectTypeEnum projectType;
}
