package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.DisbursementScheduleRequestDTO;
import br.com.gopro.api.dtos.DisbursementScheduleResponseDTO;
import br.com.gopro.api.dtos.DisbursementScheduleUpdateDTO;
import br.com.gopro.api.model.DisbursementSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DisbursementScheduleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "project.id", source = "projectId")
    DisbursementSchedule toEntity(DisbursementScheduleRequestDTO dto);

    @Mapping(target = "projectId", source = "project.id")
    DisbursementScheduleResponseDTO toDTO(DisbursementSchedule disbursementSchedule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "project.id", source = "projectId")
    void updateEntityFromDTO(DisbursementScheduleUpdateDTO dto, @MappingTarget DisbursementSchedule disbursementSchedule);
}