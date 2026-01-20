package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;
import br.com.gopro.api.mapper.BudgetItemMapper;
import br.com.gopro.api.model.BudgetCategories;
import br.com.gopro.api.model.BudgetItems;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.BudgetCategoriesRepository;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetItemServiceImpl implements BudgetItemService{

    private BudgetItemRepository budgetItemRepository;
    private BudgetCategoriesRepository budgetCategoriesRepository;
    private ProjectRepository projectRepository;
    private BudgetItemMapper budgetItemMapper;

    @Override
    public BudgetItemResponseDTO createBudgetItem(BudgetItemRequestDTO dto) {
        BudgetItems budgetItems = budgetItemMapper.toEntity(dto);

        budgetItems.setProject(findProjectById(dto.project()));
        budgetItems.setBudgetCategories(findBudgetCategoriesById(dto.BudgetCategories()));

        BudgetItems budgetItemsCreated = budgetItemRepository.save(budgetItems);

        return budgetItemMapper.toDTO(budgetItemsCreated);
    }

    @Override
    public List<BudgetItemResponseDTO> listAllBudgetItems() {
        return budgetItemRepository.findAll().stream()
                .map(budgetItemMapper::toDTO)
                .toList();
    }

    @Override
    public BudgetItemResponseDTO findBudgetItemById(Long id) {
        BudgetItems budgetItems = budgetItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Budget Item não encontrado na base"));

        return budgetItemMapper.toDTO(budgetItems);
    }

    @Transactional
    @Override
    public BudgetItemResponseDTO updateBudgetItemById(Long id, BudgetItemRequestDTO dto) {
        BudgetItems budgetItems = budgetItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Budget Item não encontrado na base"));

        budgetItems.setProject(findProjectById(dto.project()));
        budgetItems.setBudgetCategories(findBudgetCategoriesById(dto.BudgetCategories()));
        budgetItems.setDescription(dto.description());
        budgetItems.setQuantity(dto.quantity());
        budgetItems.setUnitCost(dto.unitCost());
        budgetItems.setPlannedAmount(dto.plannedAmount());
        budgetItems.setExecutedAmount(dto.executedAmount());
        budgetItems.setNotes(dto.notes());

        BudgetItems budgetItemsUpdated = budgetItemRepository.save(budgetItems);

        return budgetItemMapper.toDTO(budgetItemsUpdated);
    }

    @Transactional
    @Override
    public void deleteBudgetItemById(Long id) {
        if (!budgetItemRepository.existsById(id)){
            throw  new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Budget Item não encontrado na base");
        }

        budgetItemRepository.deleteById(id);
    }

    private Project findProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Projeto não encontrado na base"));
    }

    private BudgetCategories findBudgetCategoriesById(Long id){
        return budgetCategoriesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Budget Categories não encontrado na base"));
    }
}
