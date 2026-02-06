package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.GoalRequestDTO;
import br.com.gopro.api.dtos.GoalResponseDTO;
import br.com.gopro.api.dtos.GoalUpdateDTO;
import br.com.gopro.api.model.Goal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GoalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "project.id", source = "projectId")
    Goal toEntity(GoalRequestDTO dto);

    @Mapping(target = "projectId", source = "project.id")
    GoalResponseDTO toDTO(Goal goal);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "project.id", source = "projectId")
    void updateEntityFromDTO(GoalUpdateDTO dto, @MappingTarget Goal goal);
}