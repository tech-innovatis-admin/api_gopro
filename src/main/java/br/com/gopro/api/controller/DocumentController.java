package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.DocumentRequestDTO;
import br.com.gopro.api.dtos.DocumentResponseDTO;
import br.com.gopro.api.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentResponseDTO> createDocument(@Valid @RequestBody DocumentRequestDTO dto) {
        DocumentResponseDTO documentCreated = documentService.createDocument(dto);
        return ResponseEntity.status(201).body(documentCreated);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> listAllDocuments(){
        return ResponseEntity.ok(documentService.listAllDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> listDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.listDocumentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequestDTO dto
    ) {
        DocumentResponseDTO documentUpdated = documentService.updateDocument(id, dto);
        return ResponseEntity.ok(documentUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
