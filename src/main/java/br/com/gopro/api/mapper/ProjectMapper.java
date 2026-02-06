package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.dtos.ProjectUpdateDTO;
import br.com.gopro.api.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "primaryPartner.id", source = "primaryPartnerId")
    @Mapping(target = "secundaryPartner.id", source = "secundaryPartnerId")
    @Mapping(target = "primaryClient.id", source = "primaryClientId")
    @Mapping(target = "secundaryClient.id", source = "secundaryClientId")
    @Mapping(target = "cordinator.id", source = "cordinatorId")
    Project toEntity(ProjectRequestDTO dto);

    @Mapping(target = "primaryPartnerId", source = "primaryPartner.id")
    @Mapping(target = "secundaryPartnerId", source = "secundaryPartner.id")
    @Mapping(target = "primaryClientId", source = "primaryClient.id")
    @Mapping(target = "secundaryClientId", source = "secundaryClient.id")
    @Mapping(target = "cordinatorId", source = "cordinator.id")
    ProjectResponseDTO toDTO(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "primaryPartner.id", source = "primaryPartnerId")
    @Mapping(target = "secundaryPartner.id", source = "secundaryPartnerId")
    @Mapping(target = "primaryClient.id", source = "primaryClientId")
    @Mapping(target = "secundaryClient.id", source = "secundaryClientId")
    @Mapping(target = "cordinator.id", source = "cordinatorId")
    void updateEntityFromDTO(ProjectUpdateDTO dto, @MappingTarget Project project);
}
