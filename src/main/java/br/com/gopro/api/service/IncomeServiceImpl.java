package br.com.gopro.api.service;

import br.com.gopro.api.dtos.IncomeRequestDTO;
import br.com.gopro.api.dtos.IncomeResponseDTO;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.IncomeMapper;
import br.com.gopro.api.model.DisbursementSchedule;
import br.com.gopro.api.model.Income;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.DisbursementScheduleRepository;
import br.com.gopro.api.repository.IncomeRepository;
import br.com.gopro.api.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService{

    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;
    private final ProjectRepository projectRepository;
    private final DisbursementScheduleRepository disbursementScheduleRepository;

    @Override
    public IncomeResponseDTO createIncome(IncomeRequestDTO dto) {
        Income income = incomeMapper.toEntity(dto);

        income.setProject(findProjectById(dto.project()));
        income.setDisbursementSchedule(findDisbursementScheduleById(dto.disbursementSchedule()));

        Income incomeCreated = incomeRepository.save(income);

        return incomeMapper.toDTO(incomeCreated);
    }

    @Override
    public List<IncomeResponseDTO> listAllIncome() {
        return incomeRepository.findAll().stream()
                .map(incomeMapper::toDTO)
                .toList();
    }

    @Override
    public IncomeResponseDTO findIncomeById(Long id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Renda não encontrada no sistema"));

        return incomeMapper.toDTO(income);
    }

    @Transactional
    @Override
    public IncomeResponseDTO updatedIncomeById(Long id, IncomeRequestDTO dto) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Renda não encontrada no sistema"));

        income.setProject(findProjectById(dto.project()));
        income.setDisbursementSchedule(findDisbursementScheduleById(dto.disbursementSchedule()));
        income.setInstallment(dto.installment());
        income.setAmount(dto.amount());
        income.setReceivedAt(dto.receivedAt());
        income.setSource(dto.source());
        income.setInvoiceNumber(dto.invoiceNumber());
        income.setNotes(dto.notes());

        Income incomeUpdated = incomeRepository.save(income);

        return incomeMapper.toDTO(incomeUpdated);
    }

    @Transactional
    @Override
    public void deleteIncomeById(Long id) {
        if (!incomeRepository.existsById(id)){
            throw new ResourceNotFoundException("Renda não encontrada no sistema");
        }

        incomeRepository.deleteById(id);
    }

    private Project findProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado na base"));
    }

    private DisbursementSchedule findDisbursementScheduleById(Long id){
        return disbursementScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cronograma de desembolso não encontrado na base"));
    }
}
