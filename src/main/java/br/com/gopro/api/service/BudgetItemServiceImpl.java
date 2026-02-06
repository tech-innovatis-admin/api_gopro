package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;
import br.com.gopro.api.dtos.BudgetItemUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.BudgetItemMapper;
import br.com.gopro.api.model.BudgetItem;
import br.com.gopro.api.repository.BudgetItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetItemServiceImpl implements BudgetItemService {

    private final BudgetItemRepository budgetItemRepository;
    private final BudgetItemMapper budgetItemMapper;

    @Override
    public BudgetItemResponseDTO createBudgetItem(BudgetItemRequestDTO dto) {
        BudgetItem budgetItem = budgetItemMapper.toEntity(dto);
        budgetItem.setIsActive(true);
        BudgetItem saved = budgetItemRepository.save(budgetItem);
        return budgetItemMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<BudgetItemResponseDTO> listAllBudgetItems(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BudgetItem> pageResult = budgetItemRepository.findByIsActiveTrue(pageable);
        List<BudgetItemResponseDTO> content = pageResult.getContent().stream()
                .map(budgetItemMapper::toDTO)
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
    public BudgetItemResponseDTO findBudgetItemById(Long id) {
        BudgetItem budgetItem = budgetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item orcamentario nao encontrado"));
        if (!Boolean.TRUE.equals(budgetItem.getIsActive())) {
            throw new ResourceNotFoundException("Item orcamentario nao encontrado");
        }
        return budgetItemMapper.toDTO(budgetItem);
    }

    @Override
    public BudgetItemResponseDTO updateBudgetItemById(Long id, BudgetItemUpdateDTO dto) {
        BudgetItem budgetItem = budgetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item orcamentario nao encontrado"));
        if (!Boolean.TRUE.equals(budgetItem.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar um item inativo");
        }
        budgetItemMapper.updateEntityFromDTO(dto, budgetItem);
        BudgetItem updated = budgetItemRepository.save(budgetItem);
        return budgetItemMapper.toDTO(updated);
    }

    @Override
    public void deleteBudgetItemById(Long id) {
        BudgetItem budgetItem = budgetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item orcamentario nao encontrado"));
        if (!Boolean.TRUE.equals(budgetItem.getIsActive())) {
            throw new BusinessException("Item orcamentario ja esta inativo");
        }
        budgetItem.setIsActive(false);
        budgetItemRepository.save(budgetItem);
    }

    @Override
    public BudgetItemResponseDTO restoreBudgetItemById(Long id) {
        BudgetItem budgetItem = budgetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item orcamentario nao encontrado"));
        if (Boolean.TRUE.equals(budgetItem.getIsActive())) {
            throw new BusinessException("Item orcamentario ja esta ativo");
        }
        budgetItem.setIsActive(true);
        BudgetItem restored = budgetItemRepository.save(budgetItem);
        return budgetItemMapper.toDTO(restored);
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