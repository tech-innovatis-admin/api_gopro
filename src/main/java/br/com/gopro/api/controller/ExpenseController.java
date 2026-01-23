package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.ExpenseRequestDTO;
import br.com.gopro.api.dtos.ExpenseResponseDTO;
import br.com.gopro.api.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Gerenciamento de despesas")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(
            summary = "Criar despesa",
            description = "Cria uma nova despesa no sistema",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Despesa criada com sucesso",
                            content = @Content(schema = @Schema(implementation = ExpenseResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> createExpense(
            @Valid @RequestBody ExpenseRequestDTO dto
    ) {
        ExpenseResponseDTO expenseCreated = expenseService.createExpense(dto);
        return ResponseEntity.status(201).body(expenseCreated);
    }

    @Operation(
            summary = "Listar despesas",
            description = "Retorna a lista de todas as despesas cadastradas",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
            }
    )
    @GetMapping
    public ResponseEntity<List<ExpenseResponseDTO>> listAllExpenses() {
        return ResponseEntity.ok(expenseService.listAllExpenses());
    }

    @Operation(
            summary = "Buscar despesa por ID",
            description = "Retorna os dados de uma despesa específica",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Despesa encontrada"),
                    @ApiResponse(responseCode = "404", description = "Despesa não encontrada", content = @Content)
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> findExpenseById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(expenseService.findExpenseById(id));
    }

    @Operation(
            summary = "Atualizar despesa",
            description = "Atualiza os dados de uma despesa existente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Despesa atualizada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Despesa não encontrada", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> updatedExpenseById(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequestDTO dto
    ) {
        ExpenseResponseDTO expenseUpdated = expenseService.updatedExpenseById(id, dto);
        return ResponseEntity.ok(expenseUpdated);
    }

    @Operation(
            summary = "Excluir despesa",
            description = "Remove uma despesa pelo ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Despesa removida com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Despesa não encontrada", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpenseById(
            @PathVariable Long id
    ) {
        expenseService.deleteExpenseById(id);
        return ResponseEntity.noContent().build();
    }
}
