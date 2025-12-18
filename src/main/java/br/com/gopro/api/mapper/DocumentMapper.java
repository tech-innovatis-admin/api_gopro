package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.DocumentRequestDTO;
import br.com.gopro.api.dtos.DocumentResponseDTO;
import br.com.gopro.api.model.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "id", ignore = true)
    Document toEntity(DocumentRequestDTO dto);

    DocumentResponseDTO toDTO(Document document);
}
