package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.IncomeRequestDTO;
import br.com.gopro.api.dtos.IncomeResponseDTO;
import br.com.gopro.api.dtos.IncomeUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.service.IncomeService;
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
@RequestMapping("/incomes")
@RequiredArgsConstructor
@Tag(name = "Incomes", description = "Gerenciamento de receitas")
public class IncomeController {

    private final IncomeService incomeService;

    @Operation(summary = "Criar receita")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Receita criada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<IncomeResponseDTO> create(@Valid @RequestBody IncomeRequestDTO dto) {
        IncomeResponseDTO created = incomeService.createIncome(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar receitas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<IncomeResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ID do projeto para filtro") @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(incomeService.listAllIncomes(page, size, projectId));
    }

    @Operation(summary = "Buscar receita por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receita encontrada"),
            @ApiResponse(responseCode = "404", description = "Receita nao encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(incomeService.findIncomeById(id));
    }

    @Operation(summary = "Atualizar receita")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receita atualizada"),
            @ApiResponse(responseCode = "404", description = "Receita nao encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody IncomeUpdateDTO dto
    ) {
        return ResponseEntity.ok(incomeService.updateIncomeById(id, dto));
    }

    @Operation(summary = "Desativar receita")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Receita desativada"),
            @ApiResponse(responseCode = "404", description = "Receita nao encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        incomeService.deleteIncomeById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar receita")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receita reativada")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<IncomeResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(incomeService.restoreIncomeById(id));
    }
}
