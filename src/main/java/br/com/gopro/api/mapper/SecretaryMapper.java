package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.SecretaryRequestDTO;
import br.com.gopro.api.dtos.SecretaryResponseDTO;
import br.com.gopro.api.dtos.SecretaryUpdateDTO;
import br.com.gopro.api.model.Secretary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SecretaryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "publicAgency.id", source = "publicAgencyId")
    Secretary toEntity(SecretaryRequestDTO dto);

    SecretaryResponseDTO toDTO(Secretary secretary);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "publicAgency.id", source = "publicAgencyId")
    void updateEntityFromDTO(SecretaryUpdateDTO dto, @MappingTarget Secretary secretary);
}
