package br.com.gopro.api.service;

import br.com.gopro.api.dtos.DocumentRequestDTO;
import br.com.gopro.api.dtos.DocumentResponseDTO;

import java.util.List;

public interface DocumentService {
    DocumentResponseDTO createDocument(DocumentRequestDTO dto);
    List<DocumentResponseDTO> listAllDocuments();
    DocumentResponseDTO listDocumentById(Long id);
    DocumentResponseDTO updateDocument(Long id, DocumentRequestDTO dto);
    void deleteDocument(Long id);
}
