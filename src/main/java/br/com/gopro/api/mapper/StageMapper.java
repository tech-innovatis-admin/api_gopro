package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.StageRequestDTO;
import br.com.gopro.api.dtos.StageResponseDTO;
import br.com.gopro.api.dtos.StageUpdateDTO;
import br.com.gopro.api.model.Stage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "goal.id", source = "goalId")
    Stage toEntity(StageRequestDTO dto);

    @Mapping(target = "goalId", source = "goal.id")
    StageResponseDTO toDTO(Stage stage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "goal.id", source = "goalId")
    void updateEntityFromDTO(StageUpdateDTO dto, @MappingTarget Stage stage);
}