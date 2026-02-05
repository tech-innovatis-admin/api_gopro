package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PartnerRequestDTO;
import br.com.gopro.api.dtos.PartnerResponseDTO;
import br.com.gopro.api.dtos.PartnerUpdateDTO;
import br.com.gopro.api.enums.PartnersTypeEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.mapper.PartnerMapper;
import br.com.gopro.api.model.Partner;
import br.com.gopro.api.repository.PartnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class +PartnerServiceImplTest {

    @Mock
    private PartnerMapper partnerMapper;

    @Mock
    private PartnerRepository partnerRepository;

    @InjectMocks
    private PartnerServiceImpl service;

    @Test
    void createPartner_shouldNormalizeAndActivateAndReturnDto() {
        PartnerRequestDTO dto = partnerRequestDTO();
        Partner partner = new Partner();
        PartnerResponseDTO responseDTO = partnerResponseDTO();

        when(partnerMapper.toEntity(dto)).thenReturn(partner);
        when(partnerRepository.save(any(Partner.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(partnerMapper.toDTO(any(Partner.class))).thenReturn(responseDTO);

        PartnerResponseDTO result = service.createPartner(dto);

        assertThat(result).isEqualTo(responseDTO);

        ArgumentCaptor<Partner> captor = ArgumentCaptor.forClass(Partner.class);
        verify(partnerRepository).save(captor.capture());
        Partner saved = captor.getValue();

        assertThat(saved.getCnpj()).isEqualTo("12345678000199");
        assertThat(saved.getPhone()).isEqualTo("11988887777");
        assertThat(saved.getIsActive()).isTrue();
    }

    @Test
    void listAllPartners_shouldThrowBusinessException_whenPageNegative() {
        assertThatThrownBy(() -> service.listAllPartners(-1, 10))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(partnerRepository, partnerMapper);
    }

    @Test
    void listAllPartners_shouldThrowBusinessException_whenSizeOutOfRange() {
        assertThatThrownBy(() -> service.listAllPartners(0, 0))
                .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> service.listAllPartners(0, 101))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(partnerRepository, partnerMapper);
    }

    @Test
    void listAllPartners_shouldReturnPageResponseMapped() {
        Partner partner = new Partner();
        PartnerResponseDTO responseDTO = partnerResponseDTO();
        PageRequest pageable = PageRequest.of(0, 10);

        when(partnerRepository.findByIsActiveTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(partner), pageable, 1));
        when(partnerMapper.toDTO(partner)).thenReturn(responseDTO);

        PageResponseDTO<PartnerResponseDTO> result = service.listAllPartners(0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0)).isEqualTo(responseDTO);
        assertThat(result.pageNumber()).isEqualTo(0);
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        verify(partnerRepository).findByIsActiveTrue(pageable);
    }

    @Test
    void findPartnerById_shouldThrowNotFound_whenMissing() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> service.findPartnerById(1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(partnerRepository).findById(1L);
        verifyNoInteractions(partnerMapper);
    }

    @Test
    void findPartnerById_shouldReturnDto_whenFound() {
        Partner partner = new Partner();
        PartnerResponseDTO responseDTO = partnerResponseDTO();

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        when(partnerMapper.toDTO(partner)).thenReturn(responseDTO);

        PartnerResponseDTO result = service.findPartnerById(1L);

        assertThat(result).isEqualTo(responseDTO);
        verify(partnerRepository).findById(1L);
        verify(partnerMapper).toDTO(partner);
    }

    @Test
    void updatePartnerById_shouldNormalizeAndReturnDto() {
        Partner partner = new Partner();
        partner.setCnpj("000");
        partner.setPhone("000");

        PartnerUpdateDTO dto = partnerUpdateDTO();
        PartnerResponseDTO responseDTO = partnerResponseDTO();

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        doNothing().when(partnerMapper).updateEntityFromDTO(dto, partner);
        when(partnerRepository.save(any(Partner.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(partnerMapper.toDTO(any(Partner.class))).thenReturn(responseDTO);

        PartnerResponseDTO result = service.updatePartnerById(1L, dto);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(partner.getCnpj()).isEqualTo("12345678000199");
        assertThat(partner.getPhone()).isEqualTo("11977776666");
        verify(partnerMapper).updateEntityFromDTO(dto, partner);
        verify(partnerRepository).save(partner);
    }

    @Test
    void updatePartnerById_shouldThrowNotFound_whenMissing() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> service.updatePartnerById(1L, partnerUpdateDTO()),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletePartnerById_shouldThrowNotFound_whenMissing() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> service.deletePartnerById(1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletePartnerById_shouldThrowBadRequest_whenAlreadyInactive() {
        Partner partner = new Partner();
        partner.setIsActive(false);

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));

        ResponseStatusException ex = catchThrowableOfType(
                () -> service.deletePartnerById(1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deletePartnerById_shouldDeactivate_whenActive() {
        Partner partner = new Partner();
        partner.setIsActive(true);

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        when(partnerRepository.save(any(Partner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deletePartnerById(1L);

        assertThat(partner.getIsActive()).isFalse();
        verify(partnerRepository).save(partner);
    }

    @Test
    void restorePartnerById_shouldThrowNotFound_whenMissing() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> service.restorePartnerById(1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void restorePartnerById_shouldThrowBadRequest_whenAlreadyActive() {
        Partner partner = new Partner();
        partner.setIsActive(true);

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));

        ResponseStatusException ex = catchThrowableOfType(
                () -> service.restorePartnerById(1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void restorePartnerById_shouldActivate_whenInactive() {
        Partner partner = new Partner();
        partner.setIsActive(false);
        PartnerResponseDTO responseDTO = partnerResponseDTO();

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        when(partnerRepository.save(any(Partner.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(partnerMapper.toDTO(any(Partner.class))).thenReturn(responseDTO);

        PartnerResponseDTO result = service.restorePartnerById(1L);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(partner.getIsActive()).isTrue();
        verify(partnerRepository).save(partner);
    }

    private PartnerRequestDTO partnerRequestDTO() {
        return new PartnerRequestDTO(
                "ACR",
                "Parceiro Exemplo",
                "Parceiro LTDA",
                PartnersTypeEnum.FUNDACAO,
                "12.345.678/0001-99",
                "parceiro@gopro.com",
                "(11) 98888-7777",
                "Rua A, 10",
                "https://gopro.com",
                "Sao Paulo",
                "SP",
                false,
                10L
        );
    }

    private PartnerUpdateDTO partnerUpdateDTO() {
        return new PartnerUpdateDTO(
                "ACR",
                "Parceiro Atualizado",
                "Parceiro LTDA",
                PartnersTypeEnum.FUNDACAO,
                "12.345.678/0001-99",
                "parceiro@gopro.com",
                "(11) 97777-6666",
                "Rua B, 20",
                "https://gopro.com",
                "Sao Paulo",
                "SP",
                true,
                20L
        );
    }

    private PartnerResponseDTO partnerResponseDTO() {
        return new PartnerResponseDTO(
                1L,
                "ACR",
                "Parceiro Exemplo",
                "Parceiro LTDA",
                PartnersTypeEnum.FUNDACAO,
                "12345678000199",
                "parceiro@gopro.com",
                "11988887777",
                "Rua A, 10",
                "https://gopro.com",
                "Sao Paulo",
                "SP",
                true,
                null,
                null,
                10L,
                null
        );
    }
}
