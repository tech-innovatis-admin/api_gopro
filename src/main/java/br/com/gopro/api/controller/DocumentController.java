package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.DocumentDownloadUrlDTO;
import br.com.gopro.api.dtos.DocumentResponseDTO;
import br.com.gopro.api.enums.DocumentOwnerTypeEnum;
import br.com.gopro.api.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Gerenciamento de documentos")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "Upload de documento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Documento criado com sucesso",
                    content = @Content(schema = @Schema(implementation = DocumentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDTO> upload(
            @Parameter(description = "Arquivo")
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Tipo do dono do documento")
            @RequestParam DocumentOwnerTypeEnum ownerType,
            @Parameter(description = "ID do dono do documento")
            @RequestParam Long ownerId,
            @Parameter(description = "Categoria opcional")
            @RequestParam(required = false) String category
    ) {
        DocumentResponseDTO document = documentService.upload(file, ownerType, ownerId, category, null);
        return ResponseEntity.status(201).body(document);
    }

    @Operation(summary = "Buscar documento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento encontrado",
                    content = @Content(schema = @Schema(implementation = DocumentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Documento nao encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.findById(id));
    }

    @Operation(summary = "Gerar URL assinada para download")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL gerada",
                    content = @Content(schema = @Schema(implementation = DocumentDownloadUrlDTO.class))),
            @ApiResponse(responseCode = "404", description = "Documento nao encontrado")
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<DocumentDownloadUrlDTO> generateDownloadUrl(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "10") long expiresInMinutes
    ) {
        DocumentDownloadUrlDTO url = documentService.generateDownloadUrl(id, Duration.ofMinutes(expiresInMinutes));
        return ResponseEntity.ok(url);
    }

    @Operation(summary = "Remover documento (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Documento removido"),
            @ApiResponse(responseCode = "404", description = "Documento nao encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
