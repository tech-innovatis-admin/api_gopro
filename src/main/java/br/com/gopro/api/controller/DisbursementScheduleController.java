package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.DisbursementScheduleRequestDTO;
import br.com.gopro.api.dtos.DisbursementScheduleResponseDTO;
import br.com.gopro.api.service.DisbursementScheduleService;
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
@RequestMapping("/disbursement-schedule")
@RequiredArgsConstructor
@Tag(name = "Disbursement Schedule", description = "Gerenciamento de cronograma de desembolsos")
public class DisbursementScheduleController {

    private final DisbursementScheduleService disbursementScheduleService;

    @Operation(summary = "Criar cronograma de desembolso")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cronograma criado com sucesso",
                    content = @Content(schema = @Schema(implementation = DisbursementScheduleResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<DisbursementScheduleResponseDTO> createDisbursementSchedule(@Valid @RequestBody DisbursementScheduleRequestDTO dto){
        DisbursementScheduleResponseDTO disbursementScheduleCreated = disbursementScheduleService.createDisbursementSchedule(dto);

        return ResponseEntity.status(201).body(disbursementScheduleCreated);
    }

    @Operation(summary = "Listar todos os cronogramas de desembolso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<DisbursementScheduleResponseDTO>> listAllDisbursementSchedule(){
        return ResponseEntity.ok(disbursementScheduleService.listAllDisbursementSchedule());
    }

    @Operation(summary = "Buscar cronograma de desembolso por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cronograma encontrado",
                    content = @Content(schema = @Schema(implementation = DisbursementScheduleResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cronograma não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DisbursementScheduleResponseDTO> findDisbursementScheduleById(@PathVariable Long id){
        return ResponseEntity.ok(disbursementScheduleService.findDisbursementScheduleById(id));
    }

    @Operation(summary = "Atualizar cronograma de desembolso por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cronograma atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = DisbursementScheduleResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cronograma não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DisbursementScheduleResponseDTO> updateDisbursementScheduleById(
            @PathVariable Long id,
            @Valid @RequestBody DisbursementScheduleRequestDTO dto
    ){
        DisbursementScheduleResponseDTO disbursementScheduleUpdated = disbursementScheduleService.updateDisbursementScheduleById(id, dto);

        return ResponseEntity.ok(disbursementScheduleUpdated);
    }

    @Operation(summary = "Excluir cronograma de desembolso por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cronograma removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cronograma não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisbursementScheduleById(@PathVariable Long id){
        disbursementScheduleService.deleteDisbursementScheduleById(id);

        return ResponseEntity.noContent().build();
    }

}
