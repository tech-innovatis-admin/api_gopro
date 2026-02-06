package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;
import br.com.gopro.api.dtos.BudgetItemUpdateDTO;
import br.com.gopro.api.model.BudgetItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BudgetItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "goal.id", source = "goalId")
    BudgetItem toEntity(BudgetItemRequestDTO dto);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "goalId", source = "goal.id")
    BudgetItemResponseDTO toDTO(BudgetItem budgetItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "goal.id", source = "goalId")
    void updateEntityFromDTO(BudgetItemUpdateDTO dto, @MappingTarget BudgetItem budgetItem);
}