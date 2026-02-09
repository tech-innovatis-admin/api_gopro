package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectStatusCategoryRequestDTO {

    @NotNull(message = "Status do projeto e obrigatorio")
    private ProjectStatusEnum projectStatus;
}
