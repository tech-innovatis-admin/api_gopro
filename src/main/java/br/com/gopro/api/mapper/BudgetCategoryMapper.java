package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.BudgetCategoryRequestDTO;
import br.com.gopro.api.dtos.BudgetCategoryResponseDTO;
import br.com.gopro.api.dtos.BudgetCategoryUpdateDTO;
import br.com.gopro.api.model.BudgetCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BudgetCategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "project.id", source = "projectId")
    BudgetCategory toEntity(BudgetCategoryRequestDTO dto);

    @Mapping(target = "projectId", source = "project.id")
    BudgetCategoryResponseDTO toDTO(BudgetCategory budgetCategory);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "project.id", source = "projectId")
    void updateEntityFromDTO(BudgetCategoryUpdateDTO dto, @MappingTarget BudgetCategory budgetCategory);
}