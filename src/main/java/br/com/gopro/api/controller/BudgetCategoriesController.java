package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.BudgetCategoriesRequestDTO;
import br.com.gopro.api.dtos.BudgetCategoriesResponseDTO;
import br.com.gopro.api.service.BudgetCategoriesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budget-categories")
@RequiredArgsConstructor
public class BudgetCategoriesController {

    private final BudgetCategoriesService budgetCategoriesService;

    @PostMapping
    public ResponseEntity<BudgetCategoriesResponseDTO> createBudgetCategories(@Valid @RequestBody BudgetCategoriesRequestDTO dto){
        BudgetCategoriesResponseDTO budgetCategoriesCreated = budgetCategoriesService.createBudgetCategories(dto);

        return ResponseEntity.status(201).body(budgetCategoriesCreated);
    }

    @GetMapping
    public ResponseEntity<List<BudgetCategoriesResponseDTO>> listAllBudgetCategories(){
        return ResponseEntity.ok(budgetCategoriesService.listAllBudgetCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetCategoriesResponseDTO> findBudgetCategoriesById(@PathVariable Long id){
        return ResponseEntity.ok(budgetCategoriesService.findBudgetCategorieById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetCategoriesResponseDTO> updateBudgetCategories(
            @PathVariable Long id,
            @Valid @RequestBody BudgetCategoriesRequestDTO dto
    ){
        BudgetCategoriesResponseDTO budgetCategoriesUpdated = budgetCategoriesService.updatedBudgetCategoriesById(id, dto);

        return ResponseEntity.ok(budgetCategoriesUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudgetCategories(@PathVariable Long id){
        budgetCategoriesService.deleteBudgetCategoriesById(id);

        return ResponseEntity.noContent().build();
    }
}
