package br.com.gopro.api.mapper;

import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.dtos.ProjectUpdateDTO;
import br.com.gopro.api.model.Partner;
import br.com.gopro.api.model.People;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.model.PublicAgency;
import br.com.gopro.api.model.Secretary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "primaryPartner", source = "primaryPartnerId", qualifiedByName = "toPartner")
    @Mapping(target = "secundaryPartner", source = "secundaryPartnerId", qualifiedByName = "toPartner")
    @Mapping(target = "primaryClient", source = "primaryClientId", qualifiedByName = "toPublicAgency")
    @Mapping(target = "secundaryClient", source = "secundaryClientId", qualifiedByName = "toSecretary")
    @Mapping(target = "cordinator", source = "cordinatorId", qualifiedByName = "toPeople")
    Project toEntity(ProjectRequestDTO dto);

    @Mapping(target = "primaryPartnerId", source = "primaryPartner.id")
    @Mapping(target = "secundaryPartnerId", source = "secundaryPartner.id")
    @Mapping(target = "primaryClientId", source = "primaryClient.id")
    @Mapping(target = "secundaryClientId", source = "secundaryClient.id")
    @Mapping(target = "cordinatorId", source = "cordinator.id")
    ProjectResponseDTO toDTO(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "primaryPartner", source = "primaryPartnerId", qualifiedByName = "toPartner")
    @Mapping(target = "secundaryPartner", source = "secundaryPartnerId", qualifiedByName = "toPartner")
    @Mapping(target = "primaryClient", source = "primaryClientId", qualifiedByName = "toPublicAgency")
    @Mapping(target = "secundaryClient", source = "secundaryClientId", qualifiedByName = "toSecretary")
    @Mapping(target = "cordinator", source = "cordinatorId", qualifiedByName = "toPeople")
    void updateEntityFromDTO(ProjectUpdateDTO dto, @MappingTarget Project project);

    @Named("toPartner")
    default Partner toPartner(Long id) {
        if (id == null) {
            return null;
        }
        Partner partner = new Partner();
        partner.setId(id);
        return partner;
    }

    @Named("toPublicAgency")
    default PublicAgency toPublicAgency(Long id) {
        if (id == null) {
            return null;
        }
        PublicAgency publicAgency = new PublicAgency();
        publicAgency.setId(id);
        return publicAgency;
    }

    @Named("toSecretary")
    default Secretary toSecretary(Long id) {
        if (id == null) {
            return null;
        }
        Secretary secretary = new Secretary();
        secretary.setId(id);
        return secretary;
    }

    @Named("toPeople")
    default People toPeople(Long id) {
        if (id == null) {
            return null;
        }
        People people = new People();
        people.setId(id);
        return people;
    }
}
