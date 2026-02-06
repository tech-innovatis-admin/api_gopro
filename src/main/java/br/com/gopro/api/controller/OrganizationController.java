package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.OrganizationRequestDTO;
import br.com.gopro.api.dtos.OrganizationResponseDTO;
import br.com.gopro.api.dtos.OrganizationUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Gerenciamento de organizacoes")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "Criar organizacao")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Organizacao criada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<OrganizationResponseDTO> create(@Valid @RequestBody OrganizationRequestDTO dto) {
        OrganizationResponseDTO created = organizationService.createOrganization(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar organizacoes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<OrganizationResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(organizationService.listAllOrganizations(page, size));
    }

    @Operation(summary = "Buscar organizacao por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organizacao encontrada"),
            @ApiResponse(responseCode = "404", description = "Organizacao nao encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(organizationService.findOrganizationById(id));
    }

    @Operation(summary = "Atualizar organizacao")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organizacao atualizada"),
            @ApiResponse(responseCode = "404", description = "Organizacao nao encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationUpdateDTO dto
    ) {
        return ResponseEntity.ok(organizationService.updateOrganizationById(id, dto));
    }

    @Operation(summary = "Desativar organizacao")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Organizacao desativada"),
            @ApiResponse(responseCode = "404", description = "Organizacao nao encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        organizationService.deleteOrganizationById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar organizacao")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organizacao reativada")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<OrganizationResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(organizationService.restoreOrganizationById(id));
    }
}