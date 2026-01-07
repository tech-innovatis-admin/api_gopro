package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.BudgetCategoriesRequestDTO;
import br.com.gopro.api.dtos.BudgetCategoriesResponseDTO;
import br.com.gopro.api.service.BudgetCategoriesService;
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
@RequestMapping("/budget-categories")
@RequiredArgsConstructor
@Tag(name = "Budget Categories", description = "Gerenciamento de categorias de orçamento")
public class BudgetCategoriesController {

    private final BudgetCategoriesService budgetCategoriesService;

    @Operation(summary = "Criar categoria de orçamento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
                    content = @Content(schema = @Schema(implementation = BudgetCategoriesResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<BudgetCategoriesResponseDTO> createBudgetCategories(@Valid @RequestBody BudgetCategoriesRequestDTO dto){
        BudgetCategoriesResponseDTO budgetCategoriesCreated = budgetCategoriesService.createBudgetCategories(dto);

        return ResponseEntity.status(201).body(budgetCategoriesCreated);
    }

    @Operation(summary = "Listar todas as categorias de orçamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<BudgetCategoriesResponseDTO>> listAllBudgetCategories(){
        return ResponseEntity.ok(budgetCategoriesService.listAllBudgetCategories());
    }

    @Operation(summary = "Buscar categoria de orçamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada",
                    content = @Content(schema = @Schema(implementation = BudgetCategoriesResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BudgetCategoriesResponseDTO> findBudgetCategoriesById(@PathVariable Long id){
        return ResponseEntity.ok(budgetCategoriesService.findBudgetCategorieById(id));
    }

    @Operation(summary = "Atualizar categoria de orçamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = BudgetCategoriesResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BudgetCategoriesResponseDTO> updateBudgetCategories(
            @PathVariable Long id,
            @Valid @RequestBody BudgetCategoriesRequestDTO dto
    ){
        BudgetCategoriesResponseDTO budgetCategoriesUpdated = budgetCategoriesService.updatedBudgetCategoriesById(id, dto);

        return ResponseEntity.ok(budgetCategoriesUpdated);
    }

    @Operation(summary = "Excluir categoria de orçamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudgetCategories(@PathVariable Long id){
        budgetCategoriesService.deleteBudgetCategoriesById(id);

        return ResponseEntity.noContent().build();
    }
}
