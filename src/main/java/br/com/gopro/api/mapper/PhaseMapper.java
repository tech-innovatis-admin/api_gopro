package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.PhaseRequestDTO;
import br.com.gopro.api.dtos.PhaseResponseDTO;
import br.com.gopro.api.dtos.PhaseUpdateDTO;
import br.com.gopro.api.model.Phase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PhaseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "stage.id", source = "stageId")
    Phase toEntity(PhaseRequestDTO dto);

    @Mapping(target = "stageId", source = "stage.id")
    PhaseResponseDTO toDTO(Phase phase);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "stage.id", source = "stageId")
    void updateEntityFromDTO(PhaseUpdateDTO dto, @MappingTarget Phase phase);
}