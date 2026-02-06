package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PartnerRequestDTO;
import br.com.gopro.api.dtos.PartnerResponseDTO;
import br.com.gopro.api.dtos.PartnerUpdateDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.PartnerMapper;
import br.com.gopro.api.model.Partner;
import br.com.gopro.api.repository.PartnerRepository;
import br.com.gopro.api.utils.NormalizeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {

    private final PartnerMapper partnerMapper;
    private final PartnerRepository partnerRepository;

    @Override
    public PartnerResponseDTO createPartner(PartnerRequestDTO dto) {
        Partner partner = partnerMapper.toEntity(dto);

        partner.setCnpj(NormalizeUtils.normalizeCnpj(dto.cnpj()));
        partner.setPhone(NormalizeUtils.normalizePhone(dto.phone()));
        partner.setIsActive(true);

        Partner partnerCreated = partnerRepository.save(partner);

        return partnerMapper.toDTO(partnerCreated);
    }

    @Override
    public PageResponseDTO<PartnerResponseDTO> listAllPartners(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Partner> partnerPage = partnerRepository.findByIsActiveTrue(pageable);

        List<PartnerResponseDTO> content = partnerPage.getContent().stream()
                .map(partnerMapper::toDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                partnerPage.getNumber(),
                partnerPage.getSize(),
                partnerPage.getTotalElements(),
                partnerPage.getTotalPages(),
                partnerPage.isFirst(),
                partnerPage.isLast()
        );
    }

    @Override
    public PartnerResponseDTO findPartnerById(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parceiro nao encontrado"));

        if (!Boolean.TRUE.equals(partner.getIsActive())) {
            throw new ResourceNotFoundException("Parceiro nao encontrado");
        }

        return partnerMapper.toDTO(partner);
    }

    @Override
    public PartnerResponseDTO updatePartnerById(Long id, PartnerUpdateDTO dto) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parceiro nao encontrado"));

        if (!Boolean.TRUE.equals(partner.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar um parceiro inativo");
        }

        partnerMapper.updateEntityFromDTO(dto, partner);

        if (dto.cnpj() != null) {
            partner.setCnpj(NormalizeUtils.normalizeCnpj(dto.cnpj()));
        }
        if (dto.phone() != null) {
            partner.setPhone(NormalizeUtils.normalizePhone(dto.phone()));
        }

        Partner partnerUpdated = partnerRepository.save(partner);

        return partnerMapper.toDTO(partnerUpdated);
    }

    @Override
    public void deletePartnerById(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parceiro nao encontrado"));

        if (!Boolean.TRUE.equals(partner.getIsActive())) {
            throw new BusinessException("Parceiro ja esta inativo");
        }

        partner.setIsActive(false);
        partnerRepository.save(partner);
    }

    @Override
    public PartnerResponseDTO restorePartnerById(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parceiro nao encontrado"));

        if (Boolean.TRUE.equals(partner.getIsActive())) {
            throw new BusinessException("Parceiro ja esta ativo");
        }

        partner.setIsActive(true);
        Partner partnerRestored = partnerRepository.save(partner);

        return partnerMapper.toDTO(partnerRestored);
    }
}
