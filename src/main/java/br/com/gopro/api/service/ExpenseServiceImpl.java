package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ExpenseRequestDTO;
import br.com.gopro.api.dtos.ExpenseResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.DocumentMapper;
import br.com.gopro.api.mapper.ExpenseMapper;
import br.com.gopro.api.model.*;
import br.com.gopro.api.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService{

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final DocumentMapper documentMapper;
    private final ProjectRepository projectRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final BudgetCategoriesRepository budgetCategoriesRepository;
    private final IncomeRepository incomeRepository;
    private final ProjectPeopleRepository projectPeopleRepository;
    private final ProjectOrganizationRepository projectOrganizationRepository;

    @Override
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto) {
        Expense expense = expenseMapper.toEntity(dto);

        expense.setProject(findProjectById(dto.project()));
        expense.setBudgetItems(findBudgetItemById(dto.budgetItems()));
        expense.setBudgetCategories(findBudgetCategorieById(dto.budgetCategories()));
        expense.setIncome(findIncomeById(dto.income()));

        verifyDocuments(dto,expense);
        validateProject(dto,expense);

        Expense expenseCreated = expenseRepository.save(expense);

        return expenseMapper.toDTO(expenseCreated);
    }

    @Override
    public List<ExpenseResponseDTO> listAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(expenseMapper::toDTO)
                .toList();
    }

    @Override
    public ExpenseResponseDTO findExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada na base de dados"));

        return expenseMapper.toDTO(expense);
    }

    @Transactional
    @Override
    public ExpenseResponseDTO updatedExpenseById(Long id, ExpenseRequestDTO dto) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada na base de dados"));

        expense.setProject(findProjectById(dto.project()));
        expense.setBudgetItems(findBudgetItemById(dto.budgetItems()));
        expense.setBudgetCategories(findBudgetCategorieById(dto.budgetCategories()));
        expense.setIncome(findIncomeById(dto.income()));
        expense.setExpenseDate(dto.expenseDate());
        expense.setQuantity(dto.quantity());
        expense.setAmount(dto.amount());
        expense.setDescription(dto.description());
        expense.setInvoiceNumber(dto.invoiceNumber());
        expense.setInvoiceDate(dto.invoiceDate());

        verifyDocuments(dto,expense);
        validateProject(dto,expense);

        Expense expenseUpdated = expenseRepository.save(expense);

        return expenseMapper.toDTO(expenseUpdated);
    }

    @Transactional
    @Override
    public void deleteExpenseById(Long id) {
        if (!expenseRepository.existsById(id)){
            throw new ResourceNotFoundException("Despesa não encontrada na base de dados");
        }

        expenseRepository.deleteById(id);
    }

    private Project findProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado na base"));
    }

    private BudgetItems findBudgetItemById(Long id){
        return budgetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado na base"));
    }

    private BudgetCategories findBudgetCategorieById(Long id){
        return budgetCategoriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada na base"));
    }

    private Income findIncomeById(Long id){
        return incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Renda não encontrada na base"));
    }

    private ProjectPeople findProjectPeopleById(Long id){
        return projectPeopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto people não encontrado na base"));
    }

    private ProjectOrganization findProjectOrganizationById(Long id){
        return projectOrganizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto Organization não encontrado na base"));
    }

    private void validateProject(ExpenseRequestDTO dto, Expense expense){
        boolean hasProjectPeople = dto.projectPeople() != null;
        boolean hasProjectOrganization = dto.projectOrganization() != null;

        if (hasProjectPeople && hasProjectOrganization){
            throw new BusinessException("Os 2 campos não podem ser preenchidos juntos");
        }

        if (!hasProjectPeople && !hasProjectOrganization){
            throw new BusinessException("Pelo menos 1 dos campos deve ser preenchido");
        }

        if (hasProjectPeople){
            expense.setProjectPeople(findProjectPeopleById(dto.projectPeople()));
            expense.setProjectOrganization(null);
            return;
        }

        expense.setProjectOrganization(findProjectOrganizationById(dto.projectOrganization()));
        expense.setProjectPeople(null);
    }

    private void verifyDocuments(ExpenseRequestDTO dto, Expense expense){
        if (dto.documents() == null){
            expense.setDocuments(List.of());
            return;
        }

        List<Document> documents = dto.documents().stream()
                .map(documentMapper::toEntity)
                .toList();

        expense.setDocuments(documents);
    }

}
