package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.BudgetTransferRequestDTO;
import br.com.gopro.api.dtos.BudgetTransferResponseDTO;
import br.com.gopro.api.dtos.BudgetTransferUpdateDTO;
import br.com.gopro.api.model.BudgetTransfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BudgetTransferMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "fromItem", ignore = true)
    @Mapping(target = "toItem", ignore = true)
    @Mapping(target = "document", ignore = true)
    BudgetTransfer toEntity(BudgetTransferRequestDTO dto);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "fromItemId", source = "fromItem.id")
    @Mapping(target = "toItemId", source = "toItem.id")
    @Mapping(target = "documentId", source = "document.id")
    BudgetTransferResponseDTO toDTO(BudgetTransfer budgetTransfer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "fromItem", ignore = true)
    @Mapping(target = "toItem", ignore = true)
    @Mapping(target = "document", ignore = true)
    void updateEntityFromDTO(BudgetTransferUpdateDTO dto, @MappingTarget BudgetTransfer budgetTransfer);
}
