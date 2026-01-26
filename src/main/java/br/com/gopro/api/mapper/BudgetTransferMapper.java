package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.BudgetTransferDTO;
import br.com.gopro.api.dtos.BudgetTransferResponseDTO;
import br.com.gopro.api.model.BudgetTransfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetTransferMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "budgetItems", ignore = true)
    @Mapping(target = "fromBudgetCategories", ignore = true)
    @Mapping(target = "toBudgetCategories", ignore = true)
    @Mapping(target = "document", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    BudgetTransfer toEntity(BudgetTransferDTO dto);

    @Mapping(source = "project.id", target = "project")
    @Mapping(source = "budgetItems.id", target = "budgetItems")
    @Mapping(source = "fromBudgetCategories.id", target = "fromBudgetCategories")
    @Mapping(source = "toBudgetCategories.id", target = "toBudgetCategories")
    BudgetTransferResponseDTO toDTO(BudgetTransfer budgetTransfer);
}
