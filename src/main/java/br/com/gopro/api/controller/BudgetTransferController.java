package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.BudgetTransferRequestDTO;
import br.com.gopro.api.dtos.BudgetTransferResponseDTO;
import br.com.gopro.api.dtos.BudgetTransferUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.service.BudgetTransferService;
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
@RequestMapping("/budget-transfers")
@RequiredArgsConstructor
@Tag(name = "BudgetTransfers", description = "Gerenciamento de remanejamentos")
public class BudgetTransferController {

    private final BudgetTransferService budgetTransferService;

    @Operation(summary = "Criar remanejamento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Remanejamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<BudgetTransferResponseDTO> create(@Valid @RequestBody BudgetTransferRequestDTO dto) {
        BudgetTransferResponseDTO created = budgetTransferService.createBudgetTransfer(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Criar remanejamento de rubrica")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transferencia de rubrica criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping("/rubrica")
    public ResponseEntity<BudgetTransferResponseDTO> createRubricaTransfer(
            @Valid @RequestBody BudgetTransferRequestDTO dto
    ) {
        BudgetTransferResponseDTO created = budgetTransferService.createBudgetTransfer(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar remanejamentos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<BudgetTransferResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ID do projeto para filtro") @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(budgetTransferService.listAllBudgetTransfers(page, size, projectId));
    }

    @Operation(summary = "Buscar remanejamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Remanejamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Remanejamento nao encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BudgetTransferResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetTransferService.findBudgetTransferById(id));
    }

    @Operation(summary = "Atualizar remanejamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Remanejamento atualizado"),
            @ApiResponse(responseCode = "404", description = "Remanejamento nao encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BudgetTransferResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody BudgetTransferUpdateDTO dto
    ) {
        return ResponseEntity.ok(budgetTransferService.updateBudgetTransferById(id, dto));
    }

    @Operation(summary = "Desativar remanejamento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Remanejamento desativado"),
            @ApiResponse(responseCode = "404", description = "Remanejamento nao encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetTransferService.deleteBudgetTransferById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar remanejamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Remanejamento reativado")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<BudgetTransferResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(budgetTransferService.restoreBudgetTransferById(id));
    }
}
