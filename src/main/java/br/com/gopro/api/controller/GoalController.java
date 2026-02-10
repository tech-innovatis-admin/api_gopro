package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.GoalRequestDTO;
import br.com.gopro.api.dtos.GoalResponseDTO;
import br.com.gopro.api.dtos.GoalUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.service.GoalService;
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
@RequestMapping("/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Gerenciamento de metas")
public class GoalController {

    private final GoalService goalService;

    @Operation(summary = "Criar meta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Meta criada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<GoalResponseDTO> create(@Valid @RequestBody GoalRequestDTO dto) {
        GoalResponseDTO created = goalService.createGoal(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar metas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<GoalResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ID do projeto para filtro") @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(goalService.listAllGoals(page, size, projectId));
    }

    @Operation(summary = "Buscar meta por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta encontrada"),
            @ApiResponse(responseCode = "404", description = "Meta nao encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.findGoalById(id));
    }

    @Operation(summary = "Atualizar meta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta atualizada"),
            @ApiResponse(responseCode = "404", description = "Meta nao encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody GoalUpdateDTO dto
    ) {
        return ResponseEntity.ok(goalService.updateGoalById(id, dto));
    }

    @Operation(summary = "Desativar meta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Meta desativada"),
            @ApiResponse(responseCode = "404", description = "Meta nao encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        goalService.deleteGoalById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar meta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta reativada")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<GoalResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.restoreGoalById(id));
    }

    @Operation(summary = "Concluir meta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta concluida")
    })
    @PatchMapping("/{id}/conclude")
    public ResponseEntity<GoalResponseDTO> conclude(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.concludeGoalById(id));
    }

    @Operation(summary = "Reabrir meta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta reaberta")
    })
    @PatchMapping("/{id}/reopen")
    public ResponseEntity<GoalResponseDTO> reopen(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.reopenGoalById(id));
    }
}
