package br.com.gopro.api.service;

import br.com.gopro.api.dtos.DisbursementScheduleRequestDTO;
import br.com.gopro.api.dtos.DisbursementScheduleResponseDTO;

import java.util.List;

public interface DisbursementScheduleService {
    DisbursementScheduleResponseDTO createDisbursementSchedule(DisbursementScheduleRequestDTO dto);
    List<DisbursementScheduleResponseDTO> listAllDisbursementSchedule();
    DisbursementScheduleResponseDTO findDisbursementScheduleById(Long id);
    DisbursementScheduleResponseDTO updateDisbursementScheduleById(Long id, DisbursementScheduleRequestDTO dto);
    void deleteDisbursementScheduleById(Long id);
}
