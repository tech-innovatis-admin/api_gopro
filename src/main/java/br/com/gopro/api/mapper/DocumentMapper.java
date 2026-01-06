package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.DocumentRequestDTO;
import br.com.gopro.api.dtos.DocumentResponseDTO;
import br.com.gopro.api.model.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(target = "id", ignore = true)
    // o campo 'project' será preenchido manualmente no serviço (DocumentServiceImpl)
    @Mapping(target = "project", ignore = true)
    Document toEntity(DocumentRequestDTO dto);

    @Mapping(target = "project", source = "project.id")
    DocumentResponseDTO toDTO(Document document);
}
