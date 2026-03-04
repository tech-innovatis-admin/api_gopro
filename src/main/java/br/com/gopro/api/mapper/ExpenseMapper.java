package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.ExpenseRequestDTO;
import br.com.gopro.api.dtos.ExpenseResponseDTO;
import br.com.gopro.api.dtos.ExpenseUpdateDTO;
import br.com.gopro.api.model.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseRequestDTO dto) {
        Expense expense = new Expense();
        expense.setExpenseDate(dto.expenseDate());
        expense.setQuantity(dto.quantity());
        expense.setAmount(dto.amount());
        expense.setDescription(dto.description());
        expense.setInvoiceNumber(dto.invoiceNumber());
        expense.setInvoiceDate(dto.invoiceDate());
        expense.setCreatedBy(dto.createdBy());
        return expense;
    }

    public ExpenseResponseDTO toDTO(Expense expense) {
        return new ExpenseResponseDTO(
                expense.getId(),
                expense.getBudgetItem() != null ? expense.getBudgetItem().getId() : null,
                expense.getCategory() != null ? expense.getCategory().getId() : null,
                expense.getIncome() != null ? expense.getIncome().getId() : null,
                expense.getExpenseDate(),
                expense.getQuantity(),
                expense.getAmount(),
                expense.getPerson() != null ? expense.getPerson().getId() : null,
                expense.getOrganization() != null ? expense.getOrganization().getId() : null,
                expense.getDescription(),
                expense.getInvoiceNumber(),
                expense.getInvoiceDate(),
                expense.getDocument() != null ? expense.getDocument().getId() : null,
                expense.getIsActive(),
                expense.getCreatedAt(),
                expense.getUpdatedAt(),
                expense.getCreatedBy(),
                expense.getUpdatedBy()
        );
    }

    public void updateEntityFromDTO(ExpenseUpdateDTO dto, Expense expense) {
        if (dto.expenseDate() != null) {
            expense.setExpenseDate(dto.expenseDate());
        }
        if (dto.quantity() != null) {
            expense.setQuantity(dto.quantity());
        }
        if (dto.amount() != null) {
            expense.setAmount(dto.amount());
        }
        if (dto.description() != null) {
            expense.setDescription(dto.description());
        }
        if (dto.invoiceNumber() != null) {
            expense.setInvoiceNumber(dto.invoiceNumber());
        }
        if (dto.invoiceDate() != null) {
            expense.setInvoiceDate(dto.invoiceDate());
        }
        if (dto.updatedBy() != null) {
            expense.setUpdatedBy(dto.updatedBy());
        }
    }
}
