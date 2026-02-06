package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.OrganizationRequestDTO;
import br.com.gopro.api.dtos.OrganizationResponseDTO;
import br.com.gopro.api.dtos.OrganizationUpdateDTO;
import br.com.gopro.api.model.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrganizationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Organization toEntity(OrganizationRequestDTO dto);

    OrganizationResponseDTO toDTO(Organization organization);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDTO(OrganizationUpdateDTO dto, @MappingTarget Organization organization);
}