package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    // estes relacionamentos são resolvidos manualmente em ProjectServiceImpl
    @Mapping(target = "orgaoFinancioador", ignore = true)
    @Mapping(target = "executingOrg", ignore = true)
    Project toEntity(ProjectRequestDTO dto);

    @Mapping(target = "orgaoFinanciador", source = "orgaoFinancioador.id")
    @Mapping(target = "executionOrg", source = "executingOrg.id")
    ProjectResponseDTO toDTO(Project project);
}
