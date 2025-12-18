package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ContentTypEnum;
import br.com.gopro.api.enums.DocumentTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentResponseDTO(
        Long id,
        DocumentTypeEnum documentType,
        Long project,
        String fileName,
        String filePath,
        ContentTypEnum contentTyp,
        Long uploadedBy,
        Integer fileSize,
        String checkSum,
        String notes
) {
}
