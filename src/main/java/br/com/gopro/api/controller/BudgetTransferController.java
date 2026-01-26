package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.BudgetTransferDTO;
import br.com.gopro.api.dtos.BudgetTransferResponseDTO;
import br.com.gopro.api.service.BudgetTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budget-transfers")
@RequiredArgsConstructor
public class BudgetTransferController {

    private final BudgetTransferService budgetTransferService;

    @PostMapping
    public ResponseEntity<BudgetTransferResponseDTO> createBudgetTransfer(@Valid @RequestBody BudgetTransferDTO dto){
        BudgetTransferResponseDTO budgetTransferCreated = budgetTransferService.createBudgetTransfer(dto);

        return ResponseEntity.status(201).body(budgetTransferCreated);
    }

    @GetMapping
    public ResponseEntity<List<BudgetTransferResponseDTO>> listAllBudgetTransfers(){
        return ResponseEntity.ok(budgetTransferService.listAllBudgetTransfers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetTransferResponseDTO> findBudgetTransferById(@PathVariable Long id){
        return ResponseEntity.ok(budgetTransferService.findBudgetTransferById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetTransferResponseDTO> updatedBudgetTransferById(
            @PathVariable Long id,
            @Valid @RequestBody BudgetTransferDTO dto
    ) {
        BudgetTransferResponseDTO budgetTransferUpdated = budgetTransferService.updateTransferBudgetById(id, dto);

        return ResponseEntity.ok(budgetTransferUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudgetTransferById(@PathVariable Long id){
        budgetTransferService.deleteTransferBudgetById(id);

        return ResponseEntity.noContent().build();
    }
}
