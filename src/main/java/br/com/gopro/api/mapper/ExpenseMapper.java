package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.ExpenseRequestDTO;
import br.com.gopro.api.dtos.ExpenseResponseDTO;
import br.com.gopro.api.dtos.ExpenseUpdateDTO;
import br.com.gopro.api.model.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExpenseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "budgetItem.id", source = "budgetItemId")
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "income.id", source = "incomeId")
    @Mapping(target = "person.id", source = "personId")
    @Mapping(target = "organization.id", source = "organizationId")
    @Mapping(target = "document.id", source = "documentId")
    Expense toEntity(ExpenseRequestDTO dto);

    @Mapping(target = "budgetItemId", source = "budgetItem.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "incomeId", source = "income.id")
    @Mapping(target = "personId", source = "person.id")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "documentId", source = "document.id")
    ExpenseResponseDTO toDTO(Expense expense);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "budgetItem.id", source = "budgetItemId")
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "income.id", source = "incomeId")
    @Mapping(target = "person.id", source = "personId")
    @Mapping(target = "organization.id", source = "organizationId")
    @Mapping(target = "document.id", source = "documentId")
    void updateEntityFromDTO(ExpenseUpdateDTO dto, @MappingTarget Expense expense);
}
