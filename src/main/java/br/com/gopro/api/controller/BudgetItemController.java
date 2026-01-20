package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;
import br.com.gopro.api.service.BudgetItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budget-item")
@RequiredArgsConstructor
public class BudgetItemController {

    private final BudgetItemService budgetItemService;

    @PostMapping
    public ResponseEntity<BudgetItemResponseDTO> createBudgetItem(@Valid @RequestBody BudgetItemRequestDTO dto){
        BudgetItemResponseDTO budgetItemCreated = budgetItemService.createBudgetItem(dto);

        return ResponseEntity.status(201).body(budgetItemCreated);
    }

    @GetMapping
    public ResponseEntity<List<BudgetItemResponseDTO>> listAllBudgetItems(){
        return ResponseEntity.ok(budgetItemService.listAllBudgetItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetItemResponseDTO> findBudgetItemById(@PathVariable Long id){
        return ResponseEntity.ok(budgetItemService.findBudgetItemById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetItemResponseDTO> updateBudgetItemById(
            @PathVariable Long id,
            @Valid @RequestBody BudgetItemRequestDTO dto
    ) {
        BudgetItemResponseDTO budgetItemUpdated = budgetItemService.updateBudgetItemById(id, dto);

        return ResponseEntity.ok(budgetItemUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudgetItemById(@PathVariable Long id){
        budgetItemService.deleteBudgetItemById(id);

        return ResponseEntity.noContent().build();
    }
}
