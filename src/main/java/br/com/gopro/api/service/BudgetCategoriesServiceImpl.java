package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetCategoriesRequestDTO;
import br.com.gopro.api.dtos.BudgetCategoriesResponseDTO;
import br.com.gopro.api.mapper.BudgetCategoriesMapper;
import br.com.gopro.api.model.BudgetCategories;
import br.com.gopro.api.repository.BudgetCategoriesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetCategoriesServiceImpl implements BudgetCategoriesService{

    private final BudgetCategoriesRepository budgetCategoriesRepository;
    private final BudgetCategoriesMapper budgetCategoriesMapper;

    @Override
    public BudgetCategoriesResponseDTO createBudgetCategories(BudgetCategoriesRequestDTO dto) {
        BudgetCategories budgetCategories = budgetCategoriesMapper.toEntity(dto);

        BudgetCategories budgetCategoriesSaved = budgetCategoriesRepository.save(budgetCategories);

        return budgetCategoriesMapper.toDTO(budgetCategoriesSaved);
    }

    @Override
    public List<BudgetCategoriesResponseDTO> listAllBudgetCategories() {
        return budgetCategoriesRepository.findAll().stream()
                .map(budgetCategoriesMapper::toDTO)
                .toList();
    }

    @Override
    public BudgetCategoriesResponseDTO findBudgetCategorieById(Long id) {
        BudgetCategories budgetCategories = budgetCategoriesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Orçamento não encontrado na base de dados"));

        return budgetCategoriesMapper.toDTO(budgetCategories);
    }

    @Transactional
    @Override
    public BudgetCategoriesResponseDTO updatedBudgetCategoriesById(Long id, BudgetCategoriesRequestDTO dto) {
        BudgetCategories budgetCategories = budgetCategoriesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Orçamento não encontrado na base de dados"));

        budgetCategories.setName(dto.name());
        budgetCategories.setDescription(dto.description());

        BudgetCategories budgetCategoriesUpdated = budgetCategoriesRepository.save(budgetCategories);

        return budgetCategoriesMapper.toDTO(budgetCategoriesUpdated);
    }

    @Transactional
    @Override
    public void deleteBudgetCategoriesById(Long id) {
        if (!budgetCategoriesRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Orçamento não encontrado na base de dados");
        }

        budgetCategoriesRepository.deleteById(id);
    }
}
