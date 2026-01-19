package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;
import br.com.gopro.api.model.BudgetItems;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "budgetCategories", ignore = true)
    BudgetItems toEntity(BudgetItemRequestDTO dto);

    @Mapping(target = "project", source = "project.id")
    @Mapping(target = "budgetCategories", source = "budgetCategories.id")
    BudgetItemResponseDTO toDTO(BudgetItems budgetItems);
}
