package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.OrganizationRequestDTO;
import br.com.gopro.api.dtos.OrganizationResponseDTO;
import br.com.gopro.api.model.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "id", ignore = true)
    Organization toEntity(OrganizationRequestDTO dto);

    OrganizationResponseDTO toDTO(Organization organization);
}
