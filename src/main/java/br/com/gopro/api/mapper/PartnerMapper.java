package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.PartnerRequestDTO;
import br.com.gopro.api.dtos.PartnerResponseDTO;
import br.com.gopro.api.dtos.PartnerUpdateDTO;
import br.com.gopro.api.model.Partner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PartnerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Partner toEntity(PartnerRequestDTO dto);

    PartnerResponseDTO toDTO(Partner partner);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(source = "acronym", target = "acronym")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "tradeName", target = "tradeName")
    @Mapping(source = "partnersType", target = "partnersType")
    @Mapping(source = "cnpj", target = "cnpj")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "site", target = "site")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "isActive", target = "isActive")
    void updateEntityFromDTO(PartnerUpdateDTO dto, @MappingTarget Partner partner);
}