package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.BudgetCategoryRequestDTO;
import br.com.gopro.api.dtos.BudgetCategoryResponseDTO;
import br.com.gopro.api.dtos.BudgetCategoryUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.service.BudgetCategoryService;
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
@RequestMapping("/budget-categories")
@RequiredArgsConstructor
@Tag(name = "BudgetCategories", description = "Gerenciamento de categorias orcamentarias")
public class BudgetCategoryController {

    private final BudgetCategoryService budgetCategoryService;

    @Operation(summary = "Criar categoria orcamentaria")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
                    content = @Content(schema = @Schema(implementation = BudgetCategoryResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<BudgetCategoryResponseDTO> create(@Valid @RequestBody BudgetCategoryRequestDTO dto) {
        BudgetCategoryResponseDTO created = budgetCategoryService.createBudgetCategory(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar categorias orcamentarias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<BudgetCategoryResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ID do projeto para filtro") @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(budgetCategoryService.listAllBudgetCategories(page, size, projectId));
    }

    @Operation(summary = "Buscar categoria orcamentaria por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BudgetCategoryResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetCategoryService.findBudgetCategoryById(id));
    }

    @Operation(summary = "Atualizar categoria orcamentaria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BudgetCategoryResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody BudgetCategoryUpdateDTO dto
    ) {
        return ResponseEntity.ok(budgetCategoryService.updateBudgetCategoryById(id, dto));
    }

    @Operation(summary = "Desativar categoria orcamentaria")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria desativada"),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetCategoryService.deleteBudgetCategoryById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar categoria orcamentaria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria reativada")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<BudgetCategoryResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(budgetCategoryService.restoreBudgetCategoryById(id));
    }
}
