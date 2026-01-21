package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.IncomeRequestDTO;
import br.com.gopro.api.dtos.IncomeResponseDTO;
import br.com.gopro.api.model.Income;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncomeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "disbursementSchedule", ignore = true)
    Income toEntity(IncomeRequestDTO dto);

    @Mapping(target = "project", source = "project.id")
    @Mapping(target = "disbursementSchedule", source = "disbursementSchedule.id")
    IncomeResponseDTO toDTO(Income income);
}
