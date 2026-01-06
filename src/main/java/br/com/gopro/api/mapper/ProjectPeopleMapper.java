package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.ProjectPeopleRequestDTO;
import br.com.gopro.api.dtos.ProjectPeopleResponseDTO;
import br.com.gopro.api.model.ProjectPeople;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectPeopleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "people", ignore = true)
    ProjectPeople toEntity(ProjectPeopleRequestDTO dto);

    @Mapping(target = "project", source = "project.id")
    @Mapping(target = "people", source = "people.id")
    ProjectPeopleResponseDTO toDTO(ProjectPeople projectPeople);
}
