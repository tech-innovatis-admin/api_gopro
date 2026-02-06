package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.ProjectPeopleRequestDTO;
import br.com.gopro.api.dtos.ProjectPeopleResponseDTO;
import br.com.gopro.api.dtos.ProjectPeopleUpdateDTO;
import br.com.gopro.api.model.ProjectPeople;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProjectPeopleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "person.id", source = "personId")
    ProjectPeople toEntity(ProjectPeopleRequestDTO dto);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "personId", source = "person.id")
    ProjectPeopleResponseDTO toDTO(ProjectPeople projectPeople);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "person.id", source = "personId")
    void updateEntityFromDTO(ProjectPeopleUpdateDTO dto, @MappingTarget ProjectPeople projectPeople);
}