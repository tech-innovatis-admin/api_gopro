package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;
import br.com.gopro.api.dtos.BudgetItemUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.service.BudgetItemService;
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
@RequestMapping("/budget-items")
@RequiredArgsConstructor
@Tag(name = "BudgetItems", description = "Gerenciamento de itens orcamentarios")
public class BudgetItemController {

    private final BudgetItemService budgetItemService;

    @Operation(summary = "Criar item orcamentario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item criado com sucesso",
                    content = @Content(schema = @Schema(implementation = BudgetItemResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<BudgetItemResponseDTO> create(@Valid @RequestBody BudgetItemRequestDTO dto) {
        BudgetItemResponseDTO created = budgetItemService.createBudgetItem(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar itens orcamentarios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<BudgetItemResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ID da categoria para filtro") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "ID do projeto para filtro") @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(budgetItemService.listAllBudgetItems(page, size, categoryId, projectId));
    }

    @Operation(summary = "Buscar item orcamentario por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item nao encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BudgetItemResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetItemService.findBudgetItemById(id));
    }

    @Operation(summary = "Atualizar item orcamentario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item nao encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BudgetItemResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody BudgetItemUpdateDTO dto
    ) {
        return ResponseEntity.ok(budgetItemService.updateBudgetItemById(id, dto));
    }

    @Operation(summary = "Desativar item orcamentario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item desativado"),
            @ApiResponse(responseCode = "404", description = "Item nao encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetItemService.deleteBudgetItemById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar item orcamentario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item reativado")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<BudgetItemResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(budgetItemService.restoreBudgetItemById(id));
    }
}
