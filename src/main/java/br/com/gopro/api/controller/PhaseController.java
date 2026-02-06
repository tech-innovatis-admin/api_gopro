package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PhaseRequestDTO;
import br.com.gopro.api.dtos.PhaseResponseDTO;
import br.com.gopro.api.dtos.PhaseUpdateDTO;
import br.com.gopro.api.service.PhaseService;
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
@RequestMapping("/phases")
@RequiredArgsConstructor
@Tag(name = "Phases", description = "Gerenciamento de fases")
public class PhaseController {

    private final PhaseService phaseService;

    @Operation(summary = "Criar fase")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fase criada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<PhaseResponseDTO> create(@Valid @RequestBody PhaseRequestDTO dto) {
        PhaseResponseDTO created = phaseService.createPhase(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar fases")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<PhaseResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(phaseService.listAllPhases(page, size));
    }

    @Operation(summary = "Buscar fase por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fase encontrada"),
            @ApiResponse(responseCode = "404", description = "Fase nao encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PhaseResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(phaseService.findPhaseById(id));
    }

    @Operation(summary = "Atualizar fase")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fase atualizada"),
            @ApiResponse(responseCode = "404", description = "Fase nao encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PhaseResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PhaseUpdateDTO dto
    ) {
        return ResponseEntity.ok(phaseService.updatePhaseById(id, dto));
    }

    @Operation(summary = "Desativar fase")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Fase desativada"),
            @ApiResponse(responseCode = "404", description = "Fase nao encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        phaseService.deletePhaseById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar fase")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fase reativada")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<PhaseResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(phaseService.restorePhaseById(id));
    }
}