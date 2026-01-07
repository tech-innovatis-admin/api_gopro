package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.ProjectOrganizationRequestDTO;
import br.com.gopro.api.dtos.ProjectOrganizationResponseDTO;
import br.com.gopro.api.model.ProjectOrganization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectOrganizationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "organization", ignore = true)
    ProjectOrganization toEntity(ProjectOrganizationRequestDTO dto);

    @Mapping(target = "project", source = "project.id")
    @Mapping(target = "organization", source = "organization.id")
    ProjectOrganizationResponseDTO toDTO(ProjectOrganization projectOrganization);
}
