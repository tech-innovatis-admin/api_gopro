package br.com.gopro.api.service;

import br.com.gopro.api.dtos.DocumentRequestDTO;
import br.com.gopro.api.dtos.DocumentResponseDTO;
import br.com.gopro.api.mapper.DocumentMapper;
import br.com.gopro.api.model.Document;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.DocumentRepository;
import br.com.gopro.api.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService{

    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final DocumentMapper documentMapper;

    @Override
    public DocumentResponseDTO createDocument(DocumentRequestDTO dto) {
        Document document = documentMapper.toEntity(dto);

        document.setProject(findProjectById(dto.project()));

        Document documentSaved = documentRepository.save(document);

        return documentMapper.toDTO(documentSaved);
    }

    @Override
    public List<DocumentResponseDTO> listAllDocuments() {
        return documentRepository.findAll().stream()
                .map(documentMapper::toDTO)
                .toList();
    }

    @Override
    public DocumentResponseDTO listDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Documento não encontrado!"));

        return documentMapper.toDTO(document);
    }

    @Transactional
    @Override
    public DocumentResponseDTO updateDocument(Long id, DocumentRequestDTO dto) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Documento não encontrado"));

        document.setDocumentType(dto.documentType());
        document.setProject(findProjectById(dto.project()));
        document.setFileName(dto.fileName());
        document.setFilePath(dto.filePath());
        document.setContentType(dto.contentTyp());
        document.setUploadedBy(dto.uploadedBy());
        document.setFileSize(dto.fileSize());
        document.setCheckSum(dto.checkSum());
        document.setNotes(dto.notes());

        Document documentUpdated = documentRepository.save(document);

        return documentMapper.toDTO(documentUpdated);
    }

    @Transactional
    @Override
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Documento não encontrado!");
        }

        documentRepository.deleteById(id);
    }

    private Project findProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Projeto não encontrado na base"));
    }
}
