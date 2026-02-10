package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetTransferRequestDTO;
import br.com.gopro.api.dtos.BudgetTransferResponseDTO;
import br.com.gopro.api.dtos.BudgetTransferUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.enums.BudgetTransferStatusEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.BudgetTransferMapper;
import br.com.gopro.api.model.BudgetTransfer;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.BudgetTransferRepository;
import br.com.gopro.api.repository.DocumentRepository;
import br.com.gopro.api.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetTransferServiceImpl implements BudgetTransferService {

    private final BudgetTransferRepository budgetTransferRepository;
    private final BudgetTransferMapper budgetTransferMapper;
    private final ProjectRepository projectRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final DocumentRepository documentRepository;

    @Override
    public BudgetTransferResponseDTO createBudgetTransfer(BudgetTransferRequestDTO dto) {
        validateCreateRequest(dto);
        BudgetTransfer budgetTransfer = budgetTransferMapper.toEntity(dto);
        attachManagedRelationsForCreate(budgetTransfer, dto);
        applyDefaults(budgetTransfer);
        budgetTransfer.setIsActive(true);
        BudgetTransfer saved = budgetTransferRepository.save(budgetTransfer);
        return budgetTransferMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<BudgetTransferResponseDTO> listAllBudgetTransfers(int page, int size, Long projectId) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BudgetTransfer> pageResult = projectId == null
                ? budgetTransferRepository.findByIsActiveTrue(pageable)
                : budgetTransferRepository.findByIsActiveTrueAndProject_Id(projectId, pageable);
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
        attachManagedRelationsForUpdate(budgetTransfer, dto);
        applyDefaults(budgetTransfer);
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

    private void validateCreateRequest(BudgetTransferRequestDTO dto) {
        if (dto.fromItemId().equals(dto.toItemId())) {
            throw new BusinessException("Item de origem deve ser diferente do item de destino");
        }

        if (dto.amount().signum() <= 0) {
            throw new BusinessException("Valor do remanejamento deve ser maior que zero");
        }

        if (!projectRepository.existsById(dto.projectId())) {
            throw new ResourceNotFoundException("Projeto nao encontrado");
        }

        if (!budgetItemRepository.existsById(dto.fromItemId())) {
            throw new ResourceNotFoundException("Item de origem nao encontrado");
        }

        if (!budgetItemRepository.existsById(dto.toItemId())) {
            throw new ResourceNotFoundException("Item de destino nao encontrado");
        }

        UUID documentId = dto.documentId();
        if (documentId != null && !documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Documento nao encontrado");
        }
    }

    private void applyDefaults(BudgetTransfer budgetTransfer) {
        if (budgetTransfer.getStatus() == null) {
            budgetTransfer.setStatus(BudgetTransferStatusEnum.APROVADO);
        }
    }

    private void attachManagedRelationsForCreate(BudgetTransfer budgetTransfer, BudgetTransferRequestDTO dto) {
        budgetTransfer.setProject(projectRepository.getReferenceById(dto.projectId()));
        budgetTransfer.setFromItem(budgetItemRepository.getReferenceById(dto.fromItemId()));
        budgetTransfer.setToItem(budgetItemRepository.getReferenceById(dto.toItemId()));
        if (dto.documentId() != null) {
            budgetTransfer.setDocument(documentRepository.getReferenceById(dto.documentId()));
        } else {
            budgetTransfer.setDocument(null);
        }
    }

    private void attachManagedRelationsForUpdate(BudgetTransfer budgetTransfer, BudgetTransferUpdateDTO dto) {
        if (dto.projectId() != null) {
            budgetTransfer.setProject(projectRepository.getReferenceById(dto.projectId()));
        }
        if (dto.fromItemId() != null) {
            budgetTransfer.setFromItem(budgetItemRepository.getReferenceById(dto.fromItemId()));
        }
        if (dto.toItemId() != null) {
            budgetTransfer.setToItem(budgetItemRepository.getReferenceById(dto.toItemId()));
        }
        if (dto.documentId() != null) {
            budgetTransfer.setDocument(documentRepository.getReferenceById(dto.documentId()));
        }
    }
}
