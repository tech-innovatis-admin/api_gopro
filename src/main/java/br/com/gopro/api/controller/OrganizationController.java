package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.OrganizationRequestDTO;
import br.com.gopro.api.dtos.OrganizationResponseDTO;
import br.com.gopro.api.service.OrganizationService;
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

import java.util.List;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Gerenciamento de organizações")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "Criar organização")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Organização criada com sucesso",
                    content = @Content(schema = @Schema(implementation = OrganizationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<OrganizationResponseDTO> createOrganization(
            @Valid @RequestBody OrganizationRequestDTO dto) {
        OrganizationResponseDTO organizationCreated = organizationService.createOrganization(dto);
        return ResponseEntity.status(201).body(organizationCreated);
    }

    @Operation(summary = "Listar todas as organizações")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<OrganizationResponseDTO>> listAllOrganizations() {
        return ResponseEntity.ok(organizationService.listAllOrganization());
    }

    @Operation(summary = "Buscar organização por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organização encontrada",
                    content = @Content(schema = @Schema(implementation = OrganizationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> listOrganizationById(
            @Parameter(description = "ID da organização", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(organizationService.listOrganizationById(id));
    }

    @Operation(summary = "Atualizar organização por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organização atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = OrganizationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> updateOrganization(
            @Parameter(description = "ID da organização", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequestDTO dto
    ){
        OrganizationResponseDTO organizationUpdated = organizationService.updateOrganization(id, dto);
        return ResponseEntity.ok(organizationUpdated);
    }

    @Operation(summary = "Excluir organização por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Organização removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(
            @Parameter(description = "ID da organização", example = "1")
            @PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }
}
