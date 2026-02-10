package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetCategoryRequestDTO;
import br.com.gopro.api.dtos.BudgetCategoryResponseDTO;
import br.com.gopro.api.dtos.BudgetCategoryUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.BudgetCategoryMapper;
import br.com.gopro.api.model.BudgetCategory;
import br.com.gopro.api.repository.BudgetCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetCategoryServiceImpl implements BudgetCategoryService {

    private final BudgetCategoryRepository budgetCategoryRepository;
    private final BudgetCategoryMapper budgetCategoryMapper;

    @Override
    public BudgetCategoryResponseDTO createBudgetCategory(BudgetCategoryRequestDTO dto) {
        BudgetCategory budgetCategory = budgetCategoryMapper.toEntity(dto);
        budgetCategory.setIsActive(true);
        BudgetCategory saved = budgetCategoryRepository.save(budgetCategory);
        return budgetCategoryMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<BudgetCategoryResponseDTO> listAllBudgetCategories(int page, int size, Long projectId) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BudgetCategory> pageResult = projectId == null
                ? budgetCategoryRepository.findByIsActiveTrue(pageable)
                : budgetCategoryRepository.findByIsActiveTrueAndProject_Id(projectId, pageable);
        List<BudgetCategoryResponseDTO> content = pageResult.getContent().stream()
                .map(budgetCategoryMapper::toDTO)
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
    public BudgetCategoryResponseDTO findBudgetCategoryById(Long id) {
        BudgetCategory budgetCategory = budgetCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria orcamentaria nao encontrada"));
        if (!Boolean.TRUE.equals(budgetCategory.getIsActive())) {
            throw new ResourceNotFoundException("Categoria orcamentaria nao encontrada");
        }
        return budgetCategoryMapper.toDTO(budgetCategory);
    }

    @Override
    public BudgetCategoryResponseDTO updateBudgetCategoryById(Long id, BudgetCategoryUpdateDTO dto) {
        BudgetCategory budgetCategory = budgetCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria orcamentaria nao encontrada"));
        if (!Boolean.TRUE.equals(budgetCategory.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar uma categoria inativa");
        }
        budgetCategoryMapper.updateEntityFromDTO(dto, budgetCategory);
        BudgetCategory updated = budgetCategoryRepository.save(budgetCategory);
        return budgetCategoryMapper.toDTO(updated);
    }

    @Override
    public void deleteBudgetCategoryById(Long id) {
        BudgetCategory budgetCategory = budgetCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria orcamentaria nao encontrada"));
        if (!Boolean.TRUE.equals(budgetCategory.getIsActive())) {
            throw new BusinessException("Categoria orcamentaria ja esta inativa");
        }
        budgetCategory.setIsActive(false);
        budgetCategoryRepository.save(budgetCategory);
    }

    @Override
    public BudgetCategoryResponseDTO restoreBudgetCategoryById(Long id) {
        BudgetCategory budgetCategory = budgetCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria orcamentaria nao encontrada"));
        if (Boolean.TRUE.equals(budgetCategory.getIsActive())) {
            throw new BusinessException("Categoria orcamentaria ja esta ativa");
        }
        budgetCategory.setIsActive(true);
        BudgetCategory restored = budgetCategoryRepository.save(budgetCategory);
        return budgetCategoryMapper.toDTO(restored);
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
