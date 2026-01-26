package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetTransferDTO;
import br.com.gopro.api.dtos.BudgetTransferResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.BudgetTransferMapper;
import br.com.gopro.api.repository.BudgetTransferRepository;
import br.com.gopro.api.mapper.DocumentMapper;
import br.com.gopro.api.model.*;
import br.com.gopro.api.repository.BudgetCategoriesRepository;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.DocumentRepository;
import br.com.gopro.api.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetTransferServiceImpl implements BudgetTransferService{

    private final BudgetTransferRepository budgetTransferRepository;
    private final BudgetTransferMapper budgetTransferMapper;
    private final ProjectRepository projectRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final BudgetCategoriesRepository budgetCategoriesRepository;
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    @Override
    public BudgetTransferResponseDTO createBudgetTransfer(BudgetTransferDTO dto) {
        BudgetTransfer budgetTransfer = budgetTransferMapper.toEntity(dto);

        budgetTransfer.setProject(findProjectById(dto.project()));
        budgetTransfer.setBudgetItems(findBudgetItemsById(dto.budgetItems()));
        budgetTransfer.setFromBudgetCategories(findBudgetCategoriesById(dto.fromBudgetCategories()));
        budgetTransfer.setToBudgetCategories(findBudgetCategoriesById(dto.toBudgetCategories()));
        verifyDocument(dto, budgetTransfer);

        BudgetTransfer budgetTransferCreated = budgetTransferRepository.save(budgetTransfer);

        return budgetTransferMapper.toDTO(budgetTransferCreated);
    }

    @Override
    public List<BudgetTransferResponseDTO> listAllBudgetTransfers() {
        return budgetTransferRepository.findAll().stream()
                .map(budgetTransferMapper::toDTO)
                .toList();
    }

    @Override
    public BudgetTransferResponseDTO findBudgetTransferById(Long id) {
        BudgetTransfer budgetTransfer = budgetTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remanejo de rúbrica não encontrado na base de dados"));

        return budgetTransferMapper.toDTO(budgetTransfer);
    }

    @Transactional
    @Override
    public BudgetTransferResponseDTO updateTransferBudgetById(Long id, BudgetTransferDTO dto) {
        BudgetTransfer budgetTransfer = budgetTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remanejo de rúbrica não encontrado na base de dados"));

        budgetTransfer.setProject(findProjectById(dto.project()));
        budgetTransfer.setBudgetItems(findBudgetItemsById(dto.budgetItems()));
        budgetTransfer.setFromBudgetCategories(findBudgetCategoriesById(dto.fromBudgetCategories()));
        budgetTransfer.setToBudgetCategories(findBudgetCategoriesById(dto.toBudgetCategories()));
        budgetTransfer.setAmount(dto.amount());
        budgetTransfer.setTransferDate(dto.transferDate());
        budgetTransfer.setBudgetTransferStatus(dto.budgetTransferStatus());
        budgetTransfer.setReason(dto.reason());
        verifyDocument(dto, budgetTransfer);

        BudgetTransfer budgetTransferUpdated = budgetTransferRepository.save(budgetTransfer);

        return budgetTransferMapper.toDTO(budgetTransferUpdated);
    }

    @Transactional
    @Override
    public void deleteTransferBudgetById(Long id) {
        if (!budgetTransferRepository.existsById(id)){
            throw new ResourceNotFoundException("Remanejo de rúbrica não encontrado na base de dados");
        }

        budgetTransferRepository.deleteById(id);
    }

    private Project findProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado na base de dados"));
    }

    private BudgetItems findBudgetItemsById(Long id){
        return budgetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado na base de dados"));
    }

    private BudgetCategories findBudgetCategoriesById(Long id){
        return budgetCategoriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rúbrica não encontrada na base de dados"));
    }

    private void verifyDocument(BudgetTransferDTO dto, BudgetTransfer budgetTransfer){
        if (dto.document() == null){
            throw new BusinessException("É necessário ter um documento vinculado ao remanejo da rúbrica");
        }
        Document document = documentMapper.toEntity(dto.document());
        documentRepository.save(document);
        budgetTransfer.setDocument(document);
    }
}
