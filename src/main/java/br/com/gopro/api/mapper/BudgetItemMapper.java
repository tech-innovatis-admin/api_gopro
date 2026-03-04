package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;
import br.com.gopro.api.dtos.BudgetItemUpdateDTO;
import br.com.gopro.api.model.BudgetCategory;
import br.com.gopro.api.model.BudgetItem;
import org.springframework.stereotype.Component;

@Component
public class BudgetItemMapper {

    public BudgetItem toEntity(BudgetItemRequestDTO dto) {
        BudgetItem budgetItem = new BudgetItem();
        budgetItem.setCategory(toCategoryReference(dto.categoryId()));
        budgetItem.setDescription(dto.description());
        budgetItem.setQuantity(dto.quantity());
        budgetItem.setMonths(dto.months());
        budgetItem.setUnitCost(dto.unitCost());
        budgetItem.setPlannedAmount(dto.plannedAmount());
        budgetItem.setExecutedAmount(dto.executedAmount());
        budgetItem.setNotes(dto.notes());
        budgetItem.setCreatedBy(dto.createdBy());
        return budgetItem;
    }

    public BudgetItemResponseDTO toDTO(BudgetItem budgetItem) {
        return new BudgetItemResponseDTO(
                budgetItem.getId(),
                budgetItem.getCategory() != null ? budgetItem.getCategory().getId() : null,
                budgetItem.getDescription(),
                budgetItem.getQuantity(),
                budgetItem.getMonths(),
                budgetItem.getUnitCost(),
                budgetItem.getPlannedAmount(),
                budgetItem.getExecutedAmount(),
                budgetItem.getGoal() != null ? budgetItem.getGoal().getId() : null,
                budgetItem.getNotes(),
                budgetItem.getIsActive(),
                budgetItem.getCreatedAt(),
                budgetItem.getUpdatedAt(),
                budgetItem.getCreatedBy(),
                budgetItem.getUpdatedBy()
        );
    }

    public void updateEntityFromDTO(BudgetItemUpdateDTO dto, BudgetItem budgetItem) {
        if (dto.categoryId() != null) {
            budgetItem.setCategory(toCategoryReference(dto.categoryId()));
        }
        if (dto.description() != null) {
            budgetItem.setDescription(dto.description());
        }
        if (dto.quantity() != null) {
            budgetItem.setQuantity(dto.quantity());
        }
        if (dto.months() != null) {
            budgetItem.setMonths(dto.months());
        }
        if (dto.unitCost() != null) {
            budgetItem.setUnitCost(dto.unitCost());
        }
        if (dto.plannedAmount() != null) {
            budgetItem.setPlannedAmount(dto.plannedAmount());
        }
        if (dto.executedAmount() != null) {
            budgetItem.setExecutedAmount(dto.executedAmount());
        }
        if (dto.notes() != null) {
            budgetItem.setNotes(dto.notes());
        }
        if (dto.updatedBy() != null) {
            budgetItem.setUpdatedBy(dto.updatedBy());
        }
    }

    private BudgetCategory toCategoryReference(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        BudgetCategory category = new BudgetCategory();
        category.setId(categoryId);
        return category;
    }
}
