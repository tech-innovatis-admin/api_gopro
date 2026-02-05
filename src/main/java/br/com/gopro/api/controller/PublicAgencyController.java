package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.*;
import br.com.gopro.api.service.PublicAgencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public-agencies")
@RequiredArgsConstructor
@Tag(name = "Public Agencies", description = "Gerenciamento de órgãos públicos")
public class PublicAgencyController {

    private final PublicAgencyService publicAgencyService;

    @Operation(summary = "Criar órgão público")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Órgão público criado com sucesso", content = @Content(schema = @Schema(implementation = PublicAgencyResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<PublicAgencyResponseDTO> createPublicAgency(@Valid @RequestBody PublicAgencyRequestDTO dto) {
        PublicAgencyResponseDTO createdPublicAgency = publicAgencyService.createPublicAgency(dto);

        return ResponseEntity.status(201).body(createdPublicAgency);
    }

    @Operation(summary = "Listar todos os órgãos públicos com paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<PublicAgencyResponseDTO>> listAllPublicAgencies(
            @Parameter(description = "Número da página (começando em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página (máximo 100)") @RequestParam(defaultValue = "10") int size) {
        PageResponseDTO<PublicAgencyResponseDTO> publicAgency = publicAgencyService.listAllPublicAgencies(page, size);
        return ResponseEntity.ok(publicAgency);
    }

    @Operation(summary = "Buscar órgão público por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Órgão público encontrado", content = @Content(schema = @Schema(implementation = PublicAgencyResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Órgão público não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PublicAgencyResponseDTO> findPublicAgencyById(@PathVariable Long id) {
        PublicAgencyResponseDTO publicAgency = publicAgencyService.findPublicAgencyById(id);
        return ResponseEntity.ok(publicAgency);
    }

    @Operation(summary = "Atualizar órgão público por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Órgão público atualizado com sucesso", content = @Content(schema = @Schema(implementation = PublicAgencyResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Órgão público não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PublicAgencyResponseDTO> updatePublicAgencyById(
            @PathVariable Long id,
            @Valid @RequestBody PublicAgencyUpdateDTO dto) {
        PublicAgencyResponseDTO updatedPublicAgency = publicAgencyService.updatePublicAgencyById(id, dto);
        return ResponseEntity.ok(updatedPublicAgency);
    }

    @Operation(summary = "Desativar órgão público por ID (Soft Delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Órgão público desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Órgão público não encontrado"),
            @ApiResponse(responseCode = "400", description = "Órgão público já está inativo")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublicAgency(@PathVariable Long id) {
        publicAgencyService.deletePublicAgencyById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar órgão público por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Órgão público reativado com sucesso", content = @Content(schema = @Schema(implementation = PublicAgencyResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Órgão público não encontrado"),
            @ApiResponse(responseCode = "400", description = "Órgão público já está ativo")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<PublicAgencyResponseDTO> restorePublicAgency(@PathVariable Long id) {
        PublicAgencyResponseDTO restoredPublicAgency = publicAgencyService.restorePublicAgencyById(id);
        return ResponseEntity.ok(restoredPublicAgency);
    }
}
