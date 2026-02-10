package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.ExpenseRequestDTO;
import br.com.gopro.api.dtos.ExpenseResponseDTO;
import br.com.gopro.api.dtos.ExpenseUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.service.ExpenseService;
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
@RequestMapping("/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Gerenciamento de despesas")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Criar despesa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Despesa criada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> create(@Valid @RequestBody ExpenseRequestDTO dto) {
        ExpenseResponseDTO created = expenseService.createExpense(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar despesas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<ExpenseResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ID do projeto para filtro") @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(expenseService.listAllExpenses(page, size, projectId));
    }

    @Operation(summary = "Buscar despesa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Despesa encontrada"),
            @ApiResponse(responseCode = "404", description = "Despesa nao encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.findExpenseById(id));
    }

    @Operation(summary = "Atualizar despesa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Despesa atualizada"),
            @ApiResponse(responseCode = "404", description = "Despesa nao encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseUpdateDTO dto
    ) {
        return ResponseEntity.ok(expenseService.updateExpenseById(id, dto));
    }

    @Operation(summary = "Desativar despesa")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Despesa desativada"),
            @ApiResponse(responseCode = "404", description = "Despesa nao encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.deleteExpenseById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar despesa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Despesa reativada")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ExpenseResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.restoreExpenseById(id));
    }
}
