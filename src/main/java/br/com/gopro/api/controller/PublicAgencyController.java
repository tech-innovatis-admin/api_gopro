package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PublicAgencyRequestDTO;
import br.com.gopro.api.dtos.PublicAgencyResponseDTO;
import br.com.gopro.api.dtos.PublicAgencyUpdateDTO;
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
@Tag(name = "Public Agencies", description = "Gerenciamento de orgaos publicos")
public class PublicAgencyController {

    private final PublicAgencyService publicAgencyService;

    @Operation(summary = "Criar orgao publico")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orgao publico criado com sucesso",
                    content = @Content(schema = @Schema(implementation = PublicAgencyResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<PublicAgencyResponseDTO> createPublicAgency(@Valid @RequestBody PublicAgencyRequestDTO dto) {
        PublicAgencyResponseDTO createdPublicAgency = publicAgencyService.createPublicAgency(dto);

        return ResponseEntity.status(201).body(createdPublicAgency);
    }

    @Operation(summary = "Listar todos os orgaos publicos com paginacao")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<PublicAgencyResponseDTO>> listAllPublicAgencies(
            @Parameter(description = "Numero da pagina (comecando em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina (maximo 100)") @RequestParam(defaultValue = "10") int size) {
        PageResponseDTO<PublicAgencyResponseDTO> publicAgency = publicAgencyService.listAllPublicAgencies(page, size);
        return ResponseEntity.ok(publicAgency);
    }

    @Operation(summary = "Buscar orgao publico por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orgao publico encontrado",
                    content = @Content(schema = @Schema(implementation = PublicAgencyResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Orgao publico nao encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PublicAgencyResponseDTO> findPublicAgencyById(@PathVariable Long id) {
        PublicAgencyResponseDTO publicAgency = publicAgencyService.findPublicAgencyById(id);
        return ResponseEntity.ok(publicAgency);
    }

    @Operation(summary = "Atualizar orgao publico por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orgao publico atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = PublicAgencyResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Orgao publico nao encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PublicAgencyResponseDTO> updatePublicAgencyById(
            @PathVariable Long id,
            @Valid @RequestBody PublicAgencyUpdateDTO dto) {
        PublicAgencyResponseDTO updatedPublicAgency = publicAgencyService.updatePublicAgencyById(id, dto);
        return ResponseEntity.ok(updatedPublicAgency);
    }

    @Operation(summary = "Desativar orgao publico por ID (Soft Delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Orgao publico desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Orgao publico nao encontrado"),
            @ApiResponse(responseCode = "400", description = "Orgao publico ja esta inativo")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublicAgency(@PathVariable Long id) {
        publicAgencyService.deletePublicAgencyById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar orgao publico por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orgao publico reativado com sucesso",
                    content = @Content(schema = @Schema(implementation = PublicAgencyResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Orgao publico nao encontrado"),
            @ApiResponse(responseCode = "400", description = "Orgao publico ja esta ativo")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<PublicAgencyResponseDTO> restorePublicAgency(@PathVariable Long id) {
        PublicAgencyResponseDTO restoredPublicAgency = publicAgencyService.restorePublicAgencyById(id);
        return ResponseEntity.ok(restoredPublicAgency);
    }
}
