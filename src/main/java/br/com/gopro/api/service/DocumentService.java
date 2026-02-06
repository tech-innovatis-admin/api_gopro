package br.com.gopro.api.service;

import br.com.gopro.api.dtos.DocumentDownloadUrlDTO;
import br.com.gopro.api.dtos.DocumentResponseDTO;
import br.com.gopro.api.enums.DocumentOwnerTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.UUID;

public interface DocumentService {
    DocumentResponseDTO upload(MultipartFile file, DocumentOwnerTypeEnum ownerType, Long ownerId, String category, Long createdBy);
    DocumentResponseDTO findById(UUID id);
    DocumentDownloadUrlDTO generateDownloadUrl(UUID id, Duration expiresIn);
    void softDelete(UUID id);
}
