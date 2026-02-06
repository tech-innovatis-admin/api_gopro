package br.com.gopro.api.service;

import br.com.gopro.api.dtos.IncomeRequestDTO;
import br.com.gopro.api.dtos.IncomeResponseDTO;
import br.com.gopro.api.dtos.IncomeUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.IncomeMapper;
import br.com.gopro.api.model.Income;
import br.com.gopro.api.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;

    @Override
    public IncomeResponseDTO createIncome(IncomeRequestDTO dto) {
        Income income = incomeMapper.toEntity(dto);
        income.setIsActive(true);
        Income saved = incomeRepository.save(income);
        return incomeMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<IncomeResponseDTO> listAllIncomes(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Income> pageResult = incomeRepository.findByIsActiveTrue(pageable);
        List<IncomeResponseDTO> content = pageResult.getContent().stream()
                .map(incomeMapper::toDTO)
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
    public IncomeResponseDTO findIncomeById(Long id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receita nao encontrada"));
        if (!Boolean.TRUE.equals(income.getIsActive())) {
            throw new ResourceNotFoundException("Receita nao encontrada");
        }
        return incomeMapper.toDTO(income);
    }

    @Override
    public IncomeResponseDTO updateIncomeById(Long id, IncomeUpdateDTO dto) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receita nao encontrada"));
        if (!Boolean.TRUE.equals(income.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar uma receita inativa");
        }
        incomeMapper.updateEntityFromDTO(dto, income);
        Income updated = incomeRepository.save(income);
        return incomeMapper.toDTO(updated);
    }

    @Override
    public void deleteIncomeById(Long id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receita nao encontrada"));
        if (!Boolean.TRUE.equals(income.getIsActive())) {
            throw new BusinessException("Receita ja esta inativa");
        }
        income.setIsActive(false);
        incomeRepository.save(income);
    }

    @Override
    public IncomeResponseDTO restoreIncomeById(Long id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receita nao encontrada"));
        if (Boolean.TRUE.equals(income.getIsActive())) {
            throw new BusinessException("Receita ja esta ativa");
        }
        income.setIsActive(true);
        Income restored = incomeRepository.save(income);
        return incomeMapper.toDTO(restored);
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