package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.ProjectCompanyRequestDTO;
import br.com.gopro.api.dtos.ProjectCompanyResponseDTO;
import br.com.gopro.api.dtos.ProjectCompanyUpdateDTO;
import br.com.gopro.api.model.ProjectCompany;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProjectCompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "company.id", source = "companyId")
    ProjectCompany toEntity(ProjectCompanyRequestDTO dto);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "companyId", source = "company.id")
    ProjectCompanyResponseDTO toDTO(ProjectCompany projectCompany);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "company.id", source = "companyId")
    void updateEntityFromDTO(ProjectCompanyUpdateDTO dto, @MappingTarget ProjectCompany projectCompany);
}