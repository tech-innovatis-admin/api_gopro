package br.com.gopro.api.service;

import br.com.gopro.api.dtos.OrganizationRequestDTO;
import br.com.gopro.api.dtos.OrganizationResponseDTO;
import br.com.gopro.api.mapper.OrganizationMapper;
import br.com.gopro.api.model.Organization;
import br.com.gopro.api.repository.OrganizationRepository;
import br.com.gopro.api.utils.NormalizeUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService{

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    public OrganizationResponseDTO createOrganization(OrganizationRequestDTO dto) {
        Organization organization = organizationMapper.toEntity(dto);

        organization.setCnpj(NormalizeUtils.normalizeCnpj(dto.cnpj()));
        organization.setPhone(NormalizeUtils.normalizePhone(dto.phone()));
        organization.setZipCode(NormalizeUtils.normalizeZipCode(dto.zipCode()));

        Organization organizationSaved = organizationRepository.save(organization);

        return organizationMapper.toDTO(organizationSaved);
    }

    @Override
    public List<OrganizationResponseDTO> listAllOrganization() {
        return organizationRepository.findAll().stream()
                .map(organizationMapper::toDTO)
                .toList();
    }

    @Override
    public OrganizationResponseDTO listOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Organização não encontrada!"));

        return organizationMapper.toDTO(organization);
    }

    @Transactional
    @Override
    public OrganizationResponseDTO updateOrganization(Long id, OrganizationRequestDTO dto) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Organização não encontrada!"));

        organization.setName(dto.name());
        organization.setCnpj(NormalizeUtils.normalizeCnpj(dto.cnpj()));
        organization.setOrganizationType(dto.organizationType());
        organization.setEmail(dto.email());
        organization.setPhone(NormalizeUtils.normalizePhone(dto.phone()));
        organization.setAddress(dto.address());
        organization.setZipCode(NormalizeUtils.normalizeZipCode(dto.zipCode()));
        organization.setCity(dto.city());
        organization.setState(dto.state());
        organization.setNotes(dto.notes());

        Organization organizationUpdated = organizationRepository.save(organization);

        return organizationMapper.toDTO(organizationUpdated);
    }

    @Transactional
    @Override
    public void deleteOrganization(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Organização não encontrada!");
        }

        organizationRepository.deleteById(id);
    }
}
