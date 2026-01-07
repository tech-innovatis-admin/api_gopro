package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.BudgetCategoriesRequestDTO;
import br.com.gopro.api.dtos.BudgetCategoriesResponseDTO;
import br.com.gopro.api.model.BudgetCategories;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetCategoriesMapper {

    @Mapping(target = "id", ignore = true)
    BudgetCategories toEntity(BudgetCategoriesRequestDTO dto);

    BudgetCategoriesResponseDTO toDTO(BudgetCategories budgetCategories);
}
