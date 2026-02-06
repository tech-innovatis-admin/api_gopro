package br.com.gopro.api.service;

import br.com.gopro.api.dtos.DisbursementScheduleRequestDTO;
import br.com.gopro.api.dtos.DisbursementScheduleResponseDTO;
import br.com.gopro.api.dtos.DisbursementScheduleUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;

public interface DisbursementScheduleService {
    DisbursementScheduleResponseDTO createDisbursementSchedule(DisbursementScheduleRequestDTO dto);
    PageResponseDTO<DisbursementScheduleResponseDTO> listAllDisbursementSchedules(int page, int size);
    DisbursementScheduleResponseDTO findDisbursementScheduleById(Long id);
    DisbursementScheduleResponseDTO updateDisbursementScheduleById(Long id, DisbursementScheduleUpdateDTO dto);
    void deleteDisbursementScheduleById(Long id);
    DisbursementScheduleResponseDTO restoreDisbursementScheduleById(Long id);
}