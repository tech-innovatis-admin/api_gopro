package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.SecretaryRequestDTO;
import br.com.gopro.api.dtos.SecretaryResponseDTO;
import br.com.gopro.api.dtos.SecretaryUpdateDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.SecretaryMapper;
import br.com.gopro.api.model.PublicAgency;
import br.com.gopro.api.model.Secretary;
import br.com.gopro.api.repository.PublicAgencyRepository;
import br.com.gopro.api.repository.SecretaryRepository;
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
public class SecretaryServiceImpl implements SecretaryService {

    private final SecretaryRepository secretaryRepository;
    private final SecretaryMapper secretaryMapper;
    private final PublicAgencyRepository publicAgencyRepository;

    @Override
    public SecretaryResponseDTO createSecretary(SecretaryRequestDTO dto) {
        Secretary secretary = secretaryMapper.toEntity(dto);

        secretary.setPublicAgency(findPublicAgencyById(dto.publicAgencyId()));
        secretary.setCnpj(NormalizeUtils.normalizeCnpj(dto.cnpj()));
        secretary.setPhone(NormalizeUtils.normalizePhone(dto.phone()));
        secretary.setContactPerson(NormalizeUtils.normalizePhone(dto.contactPerson()));
        secretary.setIsActive(true);

        Secretary createdSecretary = secretaryRepository.save(secretary);

        return secretaryMapper.toDTO(createdSecretary);
    }

    @Override
    public PageResponseDTO<SecretaryResponseDTO> listAllSecretary(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Secretary> secretaryPage = secretaryRepository.findByIsActiveTrue(pageable);

        List<SecretaryResponseDTO> content = secretaryPage.getContent().stream()
                .map(secretaryMapper::toDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                secretaryPage.getNumber(),
                secretaryPage.getSize(),
                secretaryPage.getTotalElements(),
                secretaryPage.getTotalPages(),
                secretaryPage.isFirst(),
                secretaryPage.isLast());
    }

    @Override
    public SecretaryResponseDTO findSecretaryById(Long id) {
        Secretary secretary = secretaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Secretaria nao encontrada"));

        if (!Boolean.TRUE.equals(secretary.getIsActive())) {
            throw new ResourceNotFoundException("Secretaria nao encontrada");
        }

        return secretaryMapper.toDTO(secretary);
    }

    @Transactional
    @Override
    public SecretaryResponseDTO updateSecretaryById(Long id, SecretaryUpdateDTO dto) {
        Secretary secretary = secretaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Secretaria nao encontrada"));
        if (!Boolean.TRUE.equals(secretary.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar uma secretaria inativa");
        }

        secretary.setCode(dto.code());
        secretary.setSigla(dto.sigla());
        secretary.setPublicAgency(findPublicAgencyById(dto.publicAgencyId()));
        secretary.setName(dto.name());
        if (dto.cnpj() != null) {
            secretary.setCnpj(NormalizeUtils.normalizeCnpj(dto.cnpj()));
        }
        secretary.setIsClient(dto.isClient());
        secretary.setEmail(dto.email());
        if (dto.phone() != null) {
            secretary.setPhone(NormalizeUtils.normalizePhone(dto.phone()));
        }
        secretary.setAddress(dto.address());
        if (dto.contactPerson() != null) {
            secretary.setContactPerson(NormalizeUtils.normalizePhone(dto.contactPerson()));
        }

        Secretary updatedSecretary = secretaryRepository.save(secretary);

        return secretaryMapper.toDTO(updatedSecretary);
    }

    @Transactional
    @Override
    public void deleteSecretaryById(Long id) {
        Secretary secretary = secretaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Secretaria nao encontrada"));
        if (!Boolean.TRUE.equals(secretary.getIsActive())) {
            throw new BusinessException("Secretaria ja esta inativa");
        }

        secretary.setIsActive(false);
        secretaryRepository.save(secretary);
    }

    @Transactional
    @Override
    public SecretaryResponseDTO restoreSecretaryById(Long id) {
        Secretary secretary = secretaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Secretaria nao encontrada"));
        if (Boolean.TRUE.equals(secretary.getIsActive())) {
            throw new BusinessException("Secretaria ja esta ativa");
        }

        secretary.setIsActive(true);
        Secretary restoredSecretary = secretaryRepository.save(secretary);

        return secretaryMapper.toDTO(restoredSecretary);
    }

    @Transactional
    @Override
    public SecretaryResponseDTO activateSecretaryAsClientById(Long id) {
        Secretary secretary = secretaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Secretaria nao encontrada"));
        if (Boolean.TRUE.equals(secretary.getIsClient())) {
            throw new BusinessException("Secretaria ja e cliente");
        }

        secretary.setIsClient(true);
        Secretary activateSecretary = secretaryRepository.save(secretary);

        return secretaryMapper.toDTO(activateSecretary);
    }

    @Transactional
    @Override
    public SecretaryResponseDTO desactivateSecretaryAsClientById(Long id) {
        Secretary secretary = secretaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Secretaria nao encontrada"));
        if (!Boolean.TRUE.equals(secretary.getIsClient())) {
            throw new BusinessException("Secretaria nao e cliente");
        }

        secretary.setIsClient(false);
        Secretary desactivatedSecretary = secretaryRepository.save(secretary);

        return secretaryMapper.toDTO(desactivatedSecretary);
    }

    private PublicAgency findPublicAgencyById(Long id) {
        return publicAgencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agencia publica nao encontrada"));
    }
}
