package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.CompanyRequestDTO;
import br.com.gopro.api.dtos.CompanyResponsiblePersonDTO;
import br.com.gopro.api.dtos.CompanyResponseDTO;
import br.com.gopro.api.dtos.CompanyUpdateDTO;
import br.com.gopro.api.model.Company;
import br.com.gopro.api.model.People;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "responsiblePerson", ignore = true)
    Company toEntity(CompanyRequestDTO dto);

    CompanyResponseDTO toDTO(Company company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "responsiblePerson", ignore = true)
    void updateEntityFromDTO(CompanyUpdateDTO dto, @MappingTarget Company company);

    CompanyResponsiblePersonDTO toResponsiblePersonDTO(People people);
}
