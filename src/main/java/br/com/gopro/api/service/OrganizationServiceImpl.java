package br.com.gopro.api.service;

import br.com.gopro.api.dtos.OrganizationRequestDTO;
import br.com.gopro.api.dtos.OrganizationResponseDTO;
import br.com.gopro.api.dtos.OrganizationUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.OrganizationMapper;
import br.com.gopro.api.model.Organization;
import br.com.gopro.api.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Override
    public OrganizationResponseDTO createOrganization(OrganizationRequestDTO dto) {
        Organization organization = organizationMapper.toEntity(dto);
        organization.setIsActive(true);
        Organization saved = organizationRepository.save(organization);
        return organizationMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<OrganizationResponseDTO> listAllOrganizations(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Organization> pageResult = organizationRepository.findByIsActiveTrue(pageable);
        List<OrganizationResponseDTO> content = pageResult.getContent().stream()
                .map(organizationMapper::toDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isFirst(),
                pageResult.isLast()
        );
    }

    @Override
    public OrganizationResponseDTO findOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizacao nao encontrada"));
        if (!Boolean.TRUE.equals(organization.getIsActive())) {
            throw new ResourceNotFoundException("Organizacao nao encontrada");
        }
        return organizationMapper.toDTO(organization);
    }

    @Override
    public OrganizationResponseDTO updateOrganizationById(Long id, OrganizationUpdateDTO dto) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizacao nao encontrada"));
        if (!Boolean.TRUE.equals(organization.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar uma organizacao inativa");
        }
        organizationMapper.updateEntityFromDTO(dto, organization);
        Organization updated = organizationRepository.save(organization);
        return organizationMapper.toDTO(updated);
    }

    @Override
    public void deleteOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizacao nao encontrada"));
        if (!Boolean.TRUE.equals(organization.getIsActive())) {
            throw new BusinessException("Organizacao ja esta inativa");
        }
        organization.setIsActive(false);
        organizationRepository.save(organization);
    }

    @Override
    public OrganizationResponseDTO restoreOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizacao nao encontrada"));
        if (Boolean.TRUE.equals(organization.getIsActive())) {
            throw new BusinessException("Organizacao ja esta ativa");
        }
        organization.setIsActive(true);
        Organization restored = organizationRepository.save(organization);
        return organizationMapper.toDTO(restored);
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }
    }
}