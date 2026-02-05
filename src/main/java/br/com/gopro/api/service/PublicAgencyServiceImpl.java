package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PublicAgencyRequestDTO;
import br.com.gopro.api.dtos.PublicAgencyResponseDTO;
import br.com.gopro.api.dtos.PublicAgencyUpdateDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.PublicAgencyMapper;
import br.com.gopro.api.model.PublicAgency;
import br.com.gopro.api.repository.PublicAgencyRepository;
import br.com.gopro.api.utils.NormalizeUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicAgencyServiceImpl implements PublicAgencyService {

    private final PublicAgencyMapper publicAgencyMapper;
    private final PublicAgencyRepository publicAgencyRepository;

    @Override
    public PublicAgencyResponseDTO createPublicAgency(PublicAgencyRequestDTO dto) {
        PublicAgency publicAgency = publicAgencyMapper.toEntity(dto);

        if (dto.cnpj() != null) {
            publicAgency.setCnpj(NormalizeUtils.normalizeCnpj(dto.cnpj()));
        }
        if (dto.phone() != null) {
            publicAgency.setPhone(NormalizeUtils.normalizePhone(dto.phone()));
        }
        if (dto.contactPerson() != null) {
            publicAgency.setContactPerson(NormalizeUtils.normalizePhone(dto.contactPerson()));
        }

        publicAgency.setIsActive(true);

        PublicAgency createdPublicAgency = publicAgencyRepository.save(publicAgency);

        return publicAgencyMapper.toDTO(createdPublicAgency);
    }

    @Override
    public PageResponseDTO<PublicAgencyResponseDTO> listAllPublicAgencies(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Página de ser maior que zero");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da página deve estar entre 1 e 100");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PublicAgency> publicAgencyPage = publicAgencyRepository.findByIsActiveTrue(pageable);

        List<PublicAgencyResponseDTO> content = publicAgencyPage.getContent().stream()
                .map(publicAgencyMapper::toDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                publicAgencyPage.getNumber(),
                publicAgencyPage.getSize(),
                publicAgencyPage.getTotalElements(),
                publicAgencyPage.getTotalPages(),
                publicAgencyPage.isFirst(),
                publicAgencyPage.isLast());
    }

    @Override
    public PublicAgencyResponseDTO findPublicAgencyById(Long id) {
        PublicAgency publicAgency = publicAgencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agência pública não encontrada na base de dados"));

        return publicAgencyMapper.toDTO(publicAgency);
    }

    @Transactional
    @Override
    public PublicAgencyResponseDTO updatePublicAgencyById(Long id, PublicAgencyUpdateDTO dto) {
        PublicAgency publicAgency = publicAgencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agência pública não encontrada na base de dados"));

        if (publicAgency.getIsActive() != true) {
            throw new BusinessException("Não tem como alterar uma Agência pública que não está ativo no sistema");
        }

        publicAgency.setCode(dto.code());
        publicAgency.setSigla(dto.sigla());
        publicAgency.setName(dto.name());
        if (dto.cnpj() != null) {
            publicAgency.setCnpj(NormalizeUtils.normalizeCnpj(dto.cnpj()));
        }
        publicAgency.setIsClient(dto.isClient());
        publicAgency.setPublicAgencyType(dto.publicAgencyType());
        publicAgency.setEmail(dto.email());
        if (dto.phone() != null) {
            publicAgency.setPhone(NormalizeUtils.normalizePhone(dto.phone()));
        }
        publicAgency.setAddress(dto.address());
        if (dto.contactPerson() != null) {
            publicAgency.setContactPerson(NormalizeUtils.normalizePhone(dto.contactPerson()));
        }
        publicAgency.setCity(dto.city());
        publicAgency.setState(dto.state());
        publicAgency.setUpdatedBy(dto.updatedBy());

        PublicAgency updatedPublicAgency = publicAgencyRepository.save(publicAgency);

        return publicAgencyMapper.toDTO(updatedPublicAgency);
    }

    @Transactional
    @Override
    public void deletePublicAgencyById(Long id) {
        PublicAgency publicAgency = publicAgencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agência pública não encontrada na base de dados"));

        if (!publicAgency.getIsActive()) {
            throw new BusinessException("Não é possível excluir agências públicas que já foram excluidas");
        }

        publicAgency.setIsActive(false);
        publicAgencyRepository.save(publicAgency);
    }

    @Transactional
    @Override
    public PublicAgencyResponseDTO restorePublicAgencyById(Long id) {
        PublicAgency publicAgency = publicAgencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agência pública não encontrada na base de dados"));

        if (publicAgency.getIsActive()) {
            throw new BusinessException("Não é possível ativar uma agência pública que já está ativada");
        }

        publicAgency.setIsActive(true);
        PublicAgency restoredPublicAgency = publicAgencyRepository.save(publicAgency);

        return publicAgencyMapper.toDTO(restoredPublicAgency);
    }
}
