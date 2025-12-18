package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ContentTypEnum;
import br.com.gopro.api.enums.DocumentTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentRequestDTO(
        DocumentTypeEnum documentType,

        @NotNull(message = "ID do projeto é obrigatório")
        Long project,

        @NotBlank(message = "Nome do arquivo é obrigatório!")
        String fileName,

        @NotBlank(message = "File Path é obrigatório!")
        String filePath,
        ContentTypEnum contentTyp,
        Long uploadedBy,
        Integer fileSize,
        String checkSum,
        String notes
) {
}
