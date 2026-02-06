package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PublicAgencyRequestDTO;
import br.com.gopro.api.dtos.PublicAgencyResponseDTO;
import br.com.gopro.api.dtos.PublicAgencyUpdateDTO;
import br.com.gopro.api.enums.PublicAgencyTypeEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.PublicAgencyMapper;
import br.com.gopro.api.model.PublicAgency;
import br.com.gopro.api.repository.PublicAgencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicAgencyServiceImplTest {

    @Mock
    private PublicAgencyMapper publicAgencyMapper;

    @Mock
    private PublicAgencyRepository publicAgencyRepository;

    @InjectMocks
    private PublicAgencyServiceImpl service;

    @Test
    void createPublicAgency_shouldNormalizeAndActivateAndReturnDto() {
        PublicAgencyRequestDTO dto = publicAgencyRequestDTO();
        PublicAgency publicAgency = new PublicAgency();
        PublicAgencyResponseDTO responseDTO = publicAgencyResponseDTO();

        when(publicAgencyMapper.toEntity(dto)).thenReturn(publicAgency);
        when(publicAgencyRepository.save(any(PublicAgency.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(publicAgencyMapper.toDTO(any(PublicAgency.class))).thenReturn(responseDTO);

        PublicAgencyResponseDTO result = service.createPublicAgency(dto);

        assertThat(result).isEqualTo(responseDTO);

        ArgumentCaptor<PublicAgency> captor = ArgumentCaptor.forClass(PublicAgency.class);
        verify(publicAgencyRepository).save(captor.capture());
        PublicAgency saved = captor.getValue();

        assertThat(saved.getCnpj()).isEqualTo("12345678000199");
        assertThat(saved.getPhone()).isEqualTo("11988887777");
        assertThat(saved.getContactPerson()).isEqualTo("11977776666");
        assertThat(saved.getIsActive()).isTrue();
    }

    @Test
    void listAllPublicAgencies_shouldThrowBusinessException_whenPageNegative() {
        assertThatThrownBy(() -> service.listAllPublicAgencies(-1, 10))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(publicAgencyRepository, publicAgencyMapper);
    }

    @Test
    void listAllPublicAgencies_shouldThrowBusinessException_whenSizeOutOfRange() {
        assertThatThrownBy(() -> service.listAllPublicAgencies(0, 0))
                .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> service.listAllPublicAgencies(0, 101))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(publicAgencyRepository, publicAgencyMapper);
    }

    @Test
    void listAllPublicAgencies_shouldReturnPageResponseMapped() {
        PublicAgency publicAgency = new PublicAgency();
        PublicAgencyResponseDTO responseDTO = publicAgencyResponseDTO();
        PageRequest pageable = PageRequest.of(0, 10);

        when(publicAgencyRepository.findByIsActiveTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(publicAgency), pageable, 1));
        when(publicAgencyMapper.toDTO(publicAgency)).thenReturn(responseDTO);

        PageResponseDTO<PublicAgencyResponseDTO> result = service.listAllPublicAgencies(0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0)).isEqualTo(responseDTO);
        assertThat(result.totalElements()).isEqualTo(1);
        verify(publicAgencyRepository).findByIsActiveTrue(pageable);
    }

    @Test
    void findPublicAgencyById_shouldThrowNotFound_whenMissing() {
        when(publicAgencyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findPublicAgencyById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatePublicAgencyById_shouldThrowNotFound_whenMissing() {
        when(publicAgencyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePublicAgencyById(1L, publicAgencyUpdateDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatePublicAgencyById_shouldThrowBusinessException_whenInactive() {
        PublicAgency publicAgency = new PublicAgency();
        publicAgency.setIsActive(false);

        when(publicAgencyRepository.findById(1L)).thenReturn(Optional.of(publicAgency));

        assertThatThrownBy(() -> service.updatePublicAgencyById(1L, publicAgencyUpdateDTO()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updatePublicAgencyById_shouldUpdateAndReturnDto_whenValid() {
        PublicAgency publicAgency = new PublicAgency();
        publicAgency.setIsActive(true);
        PublicAgencyUpdateDTO dto = publicAgencyUpdateDTO();
        PublicAgencyResponseDTO responseDTO = publicAgencyResponseDTO();

        when(publicAgencyRepository.findById(1L)).thenReturn(Optional.of(publicAgency));
        when(publicAgencyRepository.save(any(PublicAgency.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(publicAgencyMapper.toDTO(any(PublicAgency.class))).thenReturn(responseDTO);

        PublicAgencyResponseDTO result = service.updatePublicAgencyById(1L, dto);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(publicAgency.getCnpj()).isEqualTo("12345678000199");
        assertThat(publicAgency.getPhone()).isEqualTo("11988887777");
        assertThat(publicAgency.getContactPerson()).isEqualTo("11977776666");
        assertThat(publicAgency.getUpdatedBy()).isEqualTo(20L);
        verify(publicAgencyRepository).save(publicAgency);
    }

    @Test
    void deletePublicAgencyById_shouldThrowNotFound_whenMissing() {
        when(publicAgencyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletePublicAgencyById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deletePublicAgencyById_shouldThrowBusinessException_whenInactive() {
        PublicAgency publicAgency = new PublicAgency();
        publicAgency.setIsActive(false);

        when(publicAgencyRepository.findById(1L)).thenReturn(Optional.of(publicAgency));

        assertThatThrownBy(() -> service.deletePublicAgencyById(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deletePublicAgencyById_shouldDeactivate_whenActive() {
        PublicAgency publicAgency = new PublicAgency();
        publicAgency.setIsActive(true);

        when(publicAgencyRepository.findById(1L)).thenReturn(Optional.of(publicAgency));
        when(publicAgencyRepository.save(any(PublicAgency.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deletePublicAgencyById(1L);

        assertThat(publicAgency.getIsActive()).isFalse();
        verify(publicAgencyRepository).save(publicAgency);
    }

    @Test
    void restorePublicAgencyById_shouldThrowNotFound_whenMissing() {
        when(publicAgencyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restorePublicAgencyById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void restorePublicAgencyById_shouldThrowBusinessException_whenAlreadyActive() {
        PublicAgency publicAgency = new PublicAgency();
        publicAgency.setIsActive(true);

        when(publicAgencyRepository.findById(1L)).thenReturn(Optional.of(publicAgency));

        assertThatThrownBy(() -> service.restorePublicAgencyById(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void restorePublicAgencyById_shouldActivate_whenInactive() {
        PublicAgency publicAgency = new PublicAgency();
        publicAgency.setIsActive(false);
        PublicAgencyResponseDTO responseDTO = publicAgencyResponseDTO();

        when(publicAgencyRepository.findById(1L)).thenReturn(Optional.of(publicAgency));
        when(publicAgencyRepository.save(any(PublicAgency.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(publicAgencyMapper.toDTO(any(PublicAgency.class))).thenReturn(responseDTO);

        PublicAgencyResponseDTO result = service.restorePublicAgencyById(1L);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(publicAgency.getIsActive()).isTrue();
    }

    private PublicAgencyRequestDTO publicAgencyRequestDTO() {
        return new PublicAgencyRequestDTO(
                "PA01",
                "PA",
                "Agencia Publica",
                "12.345.678/0001-99",
                true,
                PublicAgencyTypeEnum.PREFEITURA,
                "agencia@gopro.com",
                "(11) 98888-7777",
                "Rua A, 10",
                "(11) 97777-6666",
                "Sao Paulo",
                "SP",
                true,
                10L
        );
    }

    private PublicAgencyUpdateDTO publicAgencyUpdateDTO() {
        return new PublicAgencyUpdateDTO(
                "PA02",
                "PA",
                "Agencia Publica Atualizada",
                "12.345.678/0001-99",
                true,
                PublicAgencyTypeEnum.PREFEITURA,
                "agencia@gopro.com",
                "(11) 98888-7777",
                "Rua B, 20",
                "(11) 97777-6666",
                "Sao Paulo",
                "SP",
                true,
                20L
        );
    }

    private PublicAgencyResponseDTO publicAgencyResponseDTO() {
        return new PublicAgencyResponseDTO(
                1L,
                "PA01",
                "PA",
                "Agencia Publica",
                "12345678000199",
                true,
                PublicAgencyTypeEnum.PREFEITURA,
                "agencia@gopro.com",
                "11988887777",
                "Rua A, 10",
                "11977776666",
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
