package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.DisbursementScheduleRequestDTO;
import br.com.gopro.api.dtos.DisbursementScheduleResponseDTO;
import br.com.gopro.api.service.DisbursementScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/disbursement-schedule")
@RequiredArgsConstructor
public class DisbursementScheduleController {

    private final DisbursementScheduleService disbursementScheduleService;

    @PostMapping
    public ResponseEntity<DisbursementScheduleResponseDTO> createDisbursementSchedule(@Valid @RequestBody DisbursementScheduleRequestDTO dto){
        DisbursementScheduleResponseDTO disbursementScheduleCreated = disbursementScheduleService.createDisbursementSchedule(dto);

        return ResponseEntity.status(201).body(disbursementScheduleCreated);
    }

    @GetMapping
    public ResponseEntity<List<DisbursementScheduleResponseDTO>> listAllDisbursementSchedule(){
        return ResponseEntity.ok(disbursementScheduleService.listAllDisbursementSchedule());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisbursementScheduleResponseDTO> findDisbursementScheduleById(@PathVariable Long id){
        return ResponseEntity.ok(disbursementScheduleService.findDisbursementScheduleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisbursementScheduleResponseDTO> updateDisbursementScheduleById(
            @PathVariable Long id,
            @Valid @RequestBody DisbursementScheduleRequestDTO dto
    ){
        DisbursementScheduleResponseDTO disbursementScheduleUpdated = disbursementScheduleService.updateDisbursementScheduleById(id, dto);

        return ResponseEntity.ok(disbursementScheduleUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisbursementScheduleById(@PathVariable Long id){
        disbursementScheduleService.deleteDisbursementScheduleById(id);

        return ResponseEntity.noContent().build();
    }

}
