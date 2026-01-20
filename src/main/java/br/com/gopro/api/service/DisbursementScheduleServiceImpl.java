package br.com.gopro.api.service;

import br.com.gopro.api.dtos.DisbursementScheduleRequestDTO;
import br.com.gopro.api.dtos.DisbursementScheduleResponseDTO;
import br.com.gopro.api.mapper.DisbursementScheduleMapper;
import br.com.gopro.api.model.DisbursementSchedule;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.DisbursementScheduleRepository;
import br.com.gopro.api.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisbursementScheduleServiceImpl implements DisbursementScheduleService{

    private final DisbursementScheduleRepository disbursementScheduleRepository;
    private final DisbursementScheduleMapper disbursementScheduleMapper;
    private final ProjectRepository projectRepository;

    @Override
    public DisbursementScheduleResponseDTO createDisbursementSchedule(DisbursementScheduleRequestDTO dto) {
        DisbursementSchedule disbursementSchedule = disbursementScheduleMapper.toEntity(dto);

        disbursementSchedule.setProject(findProjectById(dto.project()));

        DisbursementSchedule disbursementScheduleCreated = disbursementScheduleRepository.save(disbursementSchedule);

        return disbursementScheduleMapper.toDTO(disbursementScheduleCreated);
    }

    @Override
    public List<DisbursementScheduleResponseDTO> listAllDisbursementSchedule() {
        return disbursementScheduleRepository.findAll().stream()
                .map(disbursementScheduleMapper::toDTO)
                .toList();
    }

    @Override
    public DisbursementScheduleResponseDTO findDisbursementScheduleById(Long id) {
        DisbursementSchedule disbursementSchedule = disbursementScheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cronograma não encontrado na base"));
        return disbursementScheduleMapper.toDTO(disbursementSchedule);
    }

    @Transactional
    @Override
    public DisbursementScheduleResponseDTO updateDisbursementScheduleById(Long id, DisbursementScheduleRequestDTO dto) {
        DisbursementSchedule disbursementSchedule = disbursementScheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cronograma não encontrado na base"));

        disbursementSchedule.setProject(findProjectById(dto.project()));
        disbursementSchedule.setExpectedMonth(dto.expectedMonth());
        disbursementSchedule.setExpectedAmount(dto.expectedAmount());
        disbursementSchedule.setStatusDisbursementSchedule(dto.statusDisbursementSchedule());
        disbursementSchedule.setNotes(dto.notes());
        disbursementSchedule.setUpdatedBy(dto.updatedBy());

        DisbursementSchedule disbursementScheduleUpdated = disbursementScheduleRepository.save(disbursementSchedule);

        return disbursementScheduleMapper.toDTO(disbursementScheduleUpdated);
    }

    @Transactional
    @Override
    public void deleteDisbursementScheduleById(Long id) {
        if (!disbursementScheduleRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Cronograma não encontrado na base");
        }

        disbursementScheduleRepository.deleteById(id);
    }

    private Project findProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Projeto não encontrado na base"));
    }
}
