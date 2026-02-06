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
    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "fromItem.id", source = "fromItemId")
    @Mapping(target = "toItem.id", source = "toItemId")
    @Mapping(target = "document.id", source = "documentId")
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
    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "fromItem.id", source = "fromItemId")
    @Mapping(target = "toItem.id", source = "toItemId")
    @Mapping(target = "document.id", source = "documentId")
    void updateEntityFromDTO(BudgetTransferUpdateDTO dto, @MappingTarget BudgetTransfer budgetTransfer);
}