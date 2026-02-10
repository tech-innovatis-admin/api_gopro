package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.StageRequestDTO;
import br.com.gopro.api.dtos.StageResponseDTO;
import br.com.gopro.api.dtos.StageUpdateDTO;
import br.com.gopro.api.service.StageService;
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
@RequestMapping("/stages")
@RequiredArgsConstructor
@Tag(name = "Stages", description = "Gerenciamento de etapas")
public class StageController {

    private final StageService stageService;

    @Operation(summary = "Criar etapa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Etapa criada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<StageResponseDTO> create(@Valid @RequestBody StageRequestDTO dto) {
        StageResponseDTO created = stageService.createStage(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar etapas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<StageResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ID da meta para filtro") @RequestParam(required = false) Long goalId,
            @Parameter(description = "ID do projeto para filtro") @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(stageService.listAllStages(page, size, goalId, projectId));
    }

    @Operation(summary = "Buscar etapa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Etapa encontrada"),
            @ApiResponse(responseCode = "404", description = "Etapa nao encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StageResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(stageService.findStageById(id));
    }

    @Operation(summary = "Atualizar etapa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Etapa atualizada"),
            @ApiResponse(responseCode = "404", description = "Etapa nao encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<StageResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody StageUpdateDTO dto
    ) {
        return ResponseEntity.ok(stageService.updateStageById(id, dto));
    }

    @Operation(summary = "Desativar etapa")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Etapa desativada"),
            @ApiResponse(responseCode = "404", description = "Etapa nao encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stageService.deleteStageById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar etapa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Etapa reativada")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<StageResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(stageService.restoreStageById(id));
    }

    @Operation(summary = "Concluir etapa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Etapa concluida")
    })
    @PatchMapping("/{id}/conclude")
    public ResponseEntity<StageResponseDTO> conclude(@PathVariable Long id) {
        return ResponseEntity.ok(stageService.concludeStageById(id));
    }

    @Operation(summary = "Reabrir etapa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Etapa reaberta")
    })
    @PatchMapping("/{id}/reopen")
    public ResponseEntity<StageResponseDTO> reopen(@PathVariable Long id) {
        return ResponseEntity.ok(stageService.reopenStageById(id));
    }
}
