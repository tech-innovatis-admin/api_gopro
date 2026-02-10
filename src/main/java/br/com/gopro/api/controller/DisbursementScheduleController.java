package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.DisbursementScheduleRequestDTO;
import br.com.gopro.api.dtos.DisbursementScheduleResponseDTO;
import br.com.gopro.api.dtos.DisbursementScheduleUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.service.DisbursementScheduleService;
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
@RequestMapping("/disbursement-schedules")
@RequiredArgsConstructor
@Tag(name = "DisbursementSchedules", description = "Gerenciamento de cronogramas de desembolso")
public class DisbursementScheduleController {

    private final DisbursementScheduleService disbursementScheduleService;

    @Operation(summary = "Criar cronograma")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cronograma criado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<DisbursementScheduleResponseDTO> create(@Valid @RequestBody DisbursementScheduleRequestDTO dto) {
        DisbursementScheduleResponseDTO created = disbursementScheduleService.createDisbursementSchedule(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar cronogramas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<DisbursementScheduleResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ID do projeto para filtro") @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(disbursementScheduleService.listAllDisbursementSchedules(page, size, projectId));
    }

    @Operation(summary = "Buscar cronograma por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cronograma encontrado"),
            @ApiResponse(responseCode = "404", description = "Cronograma nao encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DisbursementScheduleResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(disbursementScheduleService.findDisbursementScheduleById(id));
    }

    @Operation(summary = "Atualizar cronograma")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cronograma atualizado"),
            @ApiResponse(responseCode = "404", description = "Cronograma nao encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DisbursementScheduleResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DisbursementScheduleUpdateDTO dto
    ) {
        return ResponseEntity.ok(disbursementScheduleService.updateDisbursementScheduleById(id, dto));
    }

    @Operation(summary = "Desativar cronograma")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cronograma desativado"),
            @ApiResponse(responseCode = "404", description = "Cronograma nao encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        disbursementScheduleService.deleteDisbursementScheduleById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar cronograma")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cronograma reativado")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<DisbursementScheduleResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(disbursementScheduleService.restoreDisbursementScheduleById(id));
    }
}
