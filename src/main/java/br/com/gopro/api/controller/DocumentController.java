package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.DocumentRequestDTO;
import br.com.gopro.api.dtos.DocumentResponseDTO;
import br.com.gopro.api.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Gerenciamento de documentos")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "Criar documento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Documento criado com sucesso",
                    content = @Content(schema = @Schema(implementation = DocumentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<DocumentResponseDTO> createDocument(@Valid @RequestBody DocumentRequestDTO dto) {
        DocumentResponseDTO documentCreated = documentService.createDocument(dto);
        return ResponseEntity.status(201).body(documentCreated);
    }

    @Operation(summary = "Listar todos os documentos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<DocumentResponseDTO>> listAllDocuments(){
        return ResponseEntity.ok(documentService.listAllDocuments());
    }

    @Operation(summary = "Buscar documento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento encontrado",
                    content = @Content(schema = @Schema(implementation = DocumentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> listDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.listDocumentById(id));
    }

    @Operation(summary = "Atualizar documento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = DocumentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequestDTO dto
    ) {
        DocumentResponseDTO documentUpdated = documentService.updateDocument(id, dto);
        return ResponseEntity.ok(documentUpdated);
    }

    @Operation(summary = "Excluir documento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Documento removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
