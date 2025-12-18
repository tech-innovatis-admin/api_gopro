package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.PeopleRequestDTO;
import br.com.gopro.api.dtos.PeopleResponseDTO;
import br.com.gopro.api.model.People;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PeopleMapper {

    @Mapping(target = "id", ignore = true)
    People toEntity(PeopleRequestDTO dto);

    PeopleResponseDTO toDTO(People people);
}
