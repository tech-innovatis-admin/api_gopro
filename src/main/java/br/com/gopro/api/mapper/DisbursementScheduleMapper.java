package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.DisbursementScheduleRequestDTO;
import br.com.gopro.api.dtos.DisbursementScheduleResponseDTO;
import br.com.gopro.api.model.DisbursementSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DisbursementScheduleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    DisbursementSchedule toEntity(DisbursementScheduleRequestDTO dto);

    @Mapping(target = "project", source = "project.id")
    DisbursementScheduleResponseDTO toDTO(DisbursementSchedule disbursementSchedule);
}
