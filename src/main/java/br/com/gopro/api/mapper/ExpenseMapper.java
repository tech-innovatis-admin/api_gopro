package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.ExpenseRequestDTO;
import br.com.gopro.api.dtos.ExpenseResponseDTO;
import br.com.gopro.api.model.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = DocumentMapper.class)
public interface ExpenseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "budgetItems", ignore = true)
    @Mapping(target = "budgetCategories", ignore = true)
    @Mapping(target = "income", ignore = true)
    @Mapping(target = "projectPeople", ignore = true)
    @Mapping(target = "projectOrganization", ignore = true)
    @Mapping(target = "documents", ignore = true)
    Expense toEntity(ExpenseRequestDTO dto);

    @Mapping(target = "project", source = "project.id")
    @Mapping(target = "budgetItems", source = "budgetItems.id")
    @Mapping(target = "budgetCategories", source = "budgetCategories.id")
    @Mapping(target = "income", source = "income.id")
    @Mapping(target = "projectPeople", source = "projectPeople.id")
    @Mapping(target = "projectOrganization", source = "projectOrganization.id")
    @Mapping(target = "documents", source = "documents")
    ExpenseResponseDTO toDTO(Expense expense);
}
