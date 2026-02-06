package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetTransferRequestDTO;
import br.com.gopro.api.dtos.BudgetTransferResponseDTO;
import br.com.gopro.api.dtos.BudgetTransferUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.BudgetTransferMapper;
import br.com.gopro.api.model.BudgetTransfer;
import br.com.gopro.api.repository.BudgetTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetTransferServiceImpl implements BudgetTransferService {

    private final BudgetTransferRepository budgetTransferRepository;
    private final BudgetTransferMapper budgetTransferMapper;

    @Override
    public BudgetTransferResponseDTO createBudgetTransfer(BudgetTransferRequestDTO dto) {
        BudgetTransfer budgetTransfer = budgetTransferMapper.toEntity(dto);
        budgetTransfer.setIsActive(true);
        BudgetTransfer saved = budgetTransferRepository.save(budgetTransfer);
        return budgetTransferMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<BudgetTransferResponseDTO> listAllBudgetTransfers(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BudgetTransfer> pageResult = budgetTransferRepository.findByIsActiveTrue(pageable);
        List<BudgetTransferResponseDTO> content = pageResult.getContent().stream()
                .map(budgetTransferMapper::toDTO)
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
    public BudgetTransferResponseDTO findBudgetTransferById(Long id) {
        BudgetTransfer budgetTransfer = budgetTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remanejamento nao encontrado"));
        if (!Boolean.TRUE.equals(budgetTransfer.getIsActive())) {
            throw new ResourceNotFoundException("Remanejamento nao encontrado");
        }
        return budgetTransferMapper.toDTO(budgetTransfer);
    }

    @Override
    public BudgetTransferResponseDTO updateBudgetTransferById(Long id, BudgetTransferUpdateDTO dto) {
        BudgetTransfer budgetTransfer = budgetTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remanejamento nao encontrado"));
        if (!Boolean.TRUE.equals(budgetTransfer.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar um remanejamento inativo");
        }
        budgetTransferMapper.updateEntityFromDTO(dto, budgetTransfer);
        BudgetTransfer updated = budgetTransferRepository.save(budgetTransfer);
        return budgetTransferMapper.toDTO(updated);
    }

    @Override
    public void deleteBudgetTransferById(Long id) {
        BudgetTransfer budgetTransfer = budgetTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remanejamento nao encontrado"));
        if (!Boolean.TRUE.equals(budgetTransfer.getIsActive())) {
            throw new BusinessException("Remanejamento ja esta inativo");
        }
        budgetTransfer.setIsActive(false);
        budgetTransferRepository.save(budgetTransfer);
    }

    @Override
    public BudgetTransferResponseDTO restoreBudgetTransferById(Long id) {
        BudgetTransfer budgetTransfer = budgetTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remanejamento nao encontrado"));
        if (Boolean.TRUE.equals(budgetTransfer.getIsActive())) {
            throw new BusinessException("Remanejamento ja esta ativo");
        }
        budgetTransfer.setIsActive(true);
        BudgetTransfer restored = budgetTransferRepository.save(budgetTransfer);
        return budgetTransferMapper.toDTO(restored);
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