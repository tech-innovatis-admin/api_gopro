package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PartnerRequestDTO;
import br.com.gopro.api.dtos.PartnerResponseDTO;
import br.com.gopro.api.dtos.PartnerUpdateDTO;
import br.com.gopro.api.service.PartnerService;
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
@RequestMapping("/partners")
@RequiredArgsConstructor
@Tag(name = "Partners", description = "Gerenciamento de parceiros")
public class PartnerController {

    private final PartnerService partnerService;

    @Operation(summary = "Criar parceiro")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Parceiro criado com sucesso",
                    content = @Content(schema = @Schema(implementation = PartnerResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<PartnerResponseDTO> createPartner(@Valid @RequestBody PartnerRequestDTO dto) {
        PartnerResponseDTO partnerCreated = partnerService.createPartner(dto);
        return ResponseEntity.status(201).body(partnerCreated);
    }

    @Operation(summary = "Listar todos os parceiros com paginacao")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<PartnerResponseDTO>> listAllPartners(
            @Parameter(description = "Numero da pagina (comecando em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina (maximo 100)")
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponseDTO<PartnerResponseDTO> partners = partnerService.listAllPartners(page, size);
        return ResponseEntity.ok(partners);
    }

    @Operation(summary = "Buscar parceiro por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parceiro encontrado",
                    content = @Content(schema = @Schema(implementation = PartnerResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Parceiro nao encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PartnerResponseDTO> findPartnerById(@PathVariable Long id) {
        PartnerResponseDTO partner = partnerService.findPartnerById(id);
        return ResponseEntity.ok(partner);
    }

    @Operation(summary = "Atualizar parceiro por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parceiro atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = PartnerResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Parceiro nao encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PartnerResponseDTO> updatePartnerById(
            @PathVariable Long id,
            @Valid @RequestBody PartnerUpdateDTO dto
    ) {
        PartnerResponseDTO partnerUpdated = partnerService.updatePartnerById(id, dto);
        return ResponseEntity.ok(partnerUpdated);
    }

    @Operation(summary = "Desativar parceiro por ID (Soft Delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Parceiro desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Parceiro nao encontrado"),
            @ApiResponse(responseCode = "400", description = "Parceiro ja esta inativo")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePartnerById(@PathVariable Long id) {
        partnerService.deletePartnerById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar parceiro por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parceiro reativado com sucesso",
                    content = @Content(schema = @Schema(implementation = PartnerResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Parceiro nao encontrado"),
            @ApiResponse(responseCode = "400", description = "Parceiro ja esta ativo")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<PartnerResponseDTO> restorePartnerById(@PathVariable Long id) {
        PartnerResponseDTO partnerRestored = partnerService.restorePartnerById(id);
        return ResponseEntity.ok(partnerRestored);
    }
}
