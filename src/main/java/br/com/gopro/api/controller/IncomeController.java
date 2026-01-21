package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.IncomeRequestDTO;
import br.com.gopro.api.dtos.IncomeResponseDTO;
import br.com.gopro.api.service.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/income")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeResponseDTO> createIncome(@Valid @RequestBody IncomeRequestDTO dto){
        IncomeResponseDTO incomeCreated = incomeService.createIncome(dto);

        return ResponseEntity.status(201).body(incomeCreated);
    }

    @GetMapping
    public ResponseEntity<List<IncomeResponseDTO>> listAllIncome(){
        return ResponseEntity.ok(incomeService.listAllIncome());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponseDTO> findIncomeById(@PathVariable Long id){
        return ResponseEntity.ok(incomeService.findIncomeById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponseDTO> updatedIncomeById(
            @PathVariable Long id,
            @Valid @RequestBody IncomeRequestDTO dto
    ){
        IncomeResponseDTO incomeUpdated = incomeService.updatedIncomeById(id, dto);

        return ResponseEntity.ok(incomeUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncomeById(@PathVariable Long id){
        incomeService.deleteIncomeById(id);

        return ResponseEntity.noContent().build();
    }
}
