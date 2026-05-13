package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;
import br.com.gopro.api.dtos.BudgetItemUpdateDTO;
import br.com.gopro.api.model.BudgetCategory;
import br.com.gopro.api.model.BudgetItem;
import br.com.gopro.api.model.ProjectCompany;
import br.com.gopro.api.model.ProjectPeople;
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
        budgetItem.setProjectPeople(toProjectPeopleReference(dto.projectPeopleId()));
        budgetItem.setProjectCompany(toProjectCompanyReference(dto.projectCompanyId()));
        budgetItem.setBeneficiaryType(dto.beneficiaryType());
        budgetItem.setContractedAmount(dto.contractedAmount());
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
                budgetItem.getProjectPeople() != null ? budgetItem.getProjectPeople().getId() : null,
                budgetItem.getProjectCompany() != null ? budgetItem.getProjectCompany().getId() : null,
                budgetItem.getBeneficiaryType(),
                budgetItem.getContractedAmount(),
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
        if (dto.projectPeopleId() != null) {
            budgetItem.setProjectPeople(toProjectPeopleReference(dto.projectPeopleId()));
        }
        if (dto.projectCompanyId() != null) {
            budgetItem.setProjectCompany(toProjectCompanyReference(dto.projectCompanyId()));
        }
        if (dto.beneficiaryType() != null) {
            budgetItem.setBeneficiaryType(dto.beneficiaryType());
        }
        if (dto.contractedAmount() != null) {
            budgetItem.setContractedAmount(dto.contractedAmount());
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

    private ProjectPeople toProjectPeopleReference(Long projectPeopleId) {
        if (projectPeopleId == null) {
            return null;
        }
        ProjectPeople projectPeople = new ProjectPeople();
        projectPeople.setId(projectPeopleId);
        return projectPeople;
    }

    private ProjectCompany toProjectCompanyReference(Long projectCompanyId) {
        if (projectCompanyId == null) {
            return null;
        }
        ProjectCompany projectCompany = new ProjectCompany();
        projectCompany.setId(projectCompanyId);
        return projectCompany;
    }
}
