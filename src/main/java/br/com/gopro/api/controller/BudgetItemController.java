package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;
import br.com.gopro.api.service.BudgetItemService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/budget-item")
@RequiredArgsConstructor
@Tag(name = "Budget Item", description = "Gerenciamento de itens de orçamento")
public class BudgetItemController {

    private final BudgetItemService budgetItemService;

    @Operation(summary = "Criar item de orçamento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item criado com sucesso",
                    content = @Content(schema = @Schema(implementation = BudgetItemResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<BudgetItemResponseDTO> createBudgetItem(@Valid @RequestBody BudgetItemRequestDTO dto){
        BudgetItemResponseDTO budgetItemCreated = budgetItemService.createBudgetItem(dto);

        return ResponseEntity.status(201).body(budgetItemCreated);
    }

    @Operation(summary = "Listar todos os itens de orçamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<BudgetItemResponseDTO>> listAllBudgetItems(){
        return ResponseEntity.ok(budgetItemService.listAllBudgetItems());
    }

    @Operation(summary = "Buscar item de orçamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado",
                    content = @Content(schema = @Schema(implementation = BudgetItemResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BudgetItemResponseDTO> findBudgetItemById(@PathVariable Long id){
        return ResponseEntity.ok(budgetItemService.findBudgetItemById(id));
    }

    @Operation(summary = "Atualizar item de orçamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = BudgetItemResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Item não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BudgetItemResponseDTO> updateBudgetItemById(
            @PathVariable Long id,
            @Valid @RequestBody BudgetItemRequestDTO dto
    ) {
        BudgetItemResponseDTO budgetItemUpdated = budgetItemService.updateBudgetItemById(id, dto);

        return ResponseEntity.ok(budgetItemUpdated);
    }

    @Operation(summary = "Excluir item de orçamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudgetItemById(@PathVariable Long id){
        budgetItemService.deleteBudgetItemById(id);

        return ResponseEntity.noContent().build();
    }
}
