package br.com.gopro.api.service;

import br.com.gopro.api.dtos.DisbursementScheduleRequestDTO;
import br.com.gopro.api.dtos.DisbursementScheduleResponseDTO;
import br.com.gopro.api.dtos.DisbursementScheduleUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.DisbursementScheduleMapper;
import br.com.gopro.api.model.DisbursementSchedule;
import br.com.gopro.api.repository.DisbursementScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisbursementScheduleServiceImpl implements DisbursementScheduleService {

    private final DisbursementScheduleRepository disbursementScheduleRepository;
    private final DisbursementScheduleMapper disbursementScheduleMapper;

    @Override
    public DisbursementScheduleResponseDTO createDisbursementSchedule(DisbursementScheduleRequestDTO dto) {
        DisbursementSchedule schedule = disbursementScheduleMapper.toEntity(dto);
        schedule.setIsActive(true);
        DisbursementSchedule saved = disbursementScheduleRepository.save(schedule);
        return disbursementScheduleMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<DisbursementScheduleResponseDTO> listAllDisbursementSchedules(int page, int size, Long projectId) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<DisbursementSchedule> pageResult = projectId == null
                ? disbursementScheduleRepository.findByIsActiveTrue(pageable)
                : disbursementScheduleRepository.findByIsActiveTrueAndProject_Id(projectId, pageable);
        List<DisbursementScheduleResponseDTO> content = pageResult.getContent().stream()
                .map(disbursementScheduleMapper::toDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isFirst(),
                pageResult.isLast()
        );
    }

    @Override
    public DisbursementScheduleResponseDTO findDisbursementScheduleById(Long id) {
        DisbursementSchedule schedule = disbursementScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cronograma nao encontrado"));
        if (!Boolean.TRUE.equals(schedule.getIsActive())) {
            throw new ResourceNotFoundException("Cronograma nao encontrado");
        }
        return disbursementScheduleMapper.toDTO(schedule);
    }

    @Override
    public DisbursementScheduleResponseDTO updateDisbursementScheduleById(Long id, DisbursementScheduleUpdateDTO dto) {
        DisbursementSchedule schedule = disbursementScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cronograma nao encontrado"));
        if (!Boolean.TRUE.equals(schedule.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar um cronograma inativo");
        }
        disbursementScheduleMapper.updateEntityFromDTO(dto, schedule);
        DisbursementSchedule updated = disbursementScheduleRepository.save(schedule);
        return disbursementScheduleMapper.toDTO(updated);
    }

    @Override
    public void deleteDisbursementScheduleById(Long id) {
        DisbursementSchedule schedule = disbursementScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cronograma nao encontrado"));
        if (!Boolean.TRUE.equals(schedule.getIsActive())) {
            throw new BusinessException("Cronograma ja esta inativo");
        }
        schedule.setIsActive(false);
        disbursementScheduleRepository.save(schedule);
    }

    @Override
    public DisbursementScheduleResponseDTO restoreDisbursementScheduleById(Long id) {
        DisbursementSchedule schedule = disbursementScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cronograma nao encontrado"));
        if (Boolean.TRUE.equals(schedule.getIsActive())) {
            throw new BusinessException("Cronograma ja esta ativo");
        }
        schedule.setIsActive(true);
        DisbursementSchedule restored = disbursementScheduleRepository.save(schedule);
        return disbursementScheduleMapper.toDTO(restored);
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }
    }
}
