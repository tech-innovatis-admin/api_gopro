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
class SecretaryServiceImplTest {

    @Mock
    private SecretaryRepository secretaryRepository;

    @Mock
    private SecretaryMapper secretaryMapper;

    @Mock
    private PublicAgencyRepository publicAgencyRepository;

    @InjectMocks
    private SecretaryServiceImpl service;

    @Test
    void createSecretary_shouldNormalizeAndActivateAndReturnDto() {
        SecretaryRequestDTO dto = secretaryRequestDTO();
        Secretary secretary = new Secretary();
        SecretaryResponseDTO responseDTO = secretaryResponseDTO();
        PublicAgency publicAgency = new PublicAgency();

        when(secretaryMapper.toEntity(dto)).thenReturn(secretary);
        when(publicAgencyRepository.findById(dto.publicAgencyId())).thenReturn(Optional.of(publicAgency));
        when(secretaryRepository.save(any(Secretary.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(secretaryMapper.toDTO(any(Secretary.class))).thenReturn(responseDTO);

        SecretaryResponseDTO result = service.createSecretary(dto);

        assertThat(result).isEqualTo(responseDTO);

        ArgumentCaptor<Secretary> captor = ArgumentCaptor.forClass(Secretary.class);
        verify(secretaryRepository).save(captor.capture());
        Secretary saved = captor.getValue();

        assertThat(saved.getPublicAgency()).isEqualTo(publicAgency);
        assertThat(saved.getCnpj()).isEqualTo("12345678000199");
        assertThat(saved.getPhone()).isEqualTo("11988887777");
        assertThat(saved.getContactPerson()).isEqualTo("11977776666");
        assertThat(saved.getIsActive()).isTrue();
    }

    @Test
    void listAllSecretary_shouldThrowBusinessException_whenPageNegative() {
        assertThatThrownBy(() -> service.listAllSecretary(-1, 10))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(secretaryRepository, secretaryMapper, publicAgencyRepository);
    }

    @Test
    void listAllSecretary_shouldThrowBusinessException_whenSizeOutOfRange() {
        assertThatThrownBy(() -> service.listAllSecretary(0, 0))
                .isInstanceOf(BusinessException.class);

        assertThatThrownBy(() -> service.listAllSecretary(0, 101))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(secretaryRepository, secretaryMapper, publicAgencyRepository);
    }

    @Test
    void listAllSecretary_shouldReturnPageResponseMapped() {
        Secretary secretary = new Secretary();
        SecretaryResponseDTO responseDTO = secretaryResponseDTO();
        PageRequest pageable = PageRequest.of(0, 10);

        when(secretaryRepository.findByIsActiveTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(secretary), pageable, 1));
        when(secretaryMapper.toDTO(secretary)).thenReturn(responseDTO);

        PageResponseDTO<SecretaryResponseDTO> result = service.listAllSecretary(0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0)).isEqualTo(responseDTO);
        assertThat(result.totalElements()).isEqualTo(1);
        verify(secretaryRepository).findByIsActiveTrue(pageable);
    }

    @Test
    void findSecretaryById_shouldThrowNotFound_whenMissing() {
        when(secretaryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findSecretaryById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateSecretaryById_shouldThrowNotFound_whenMissing() {
        when(secretaryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSecretaryById(1L, secretaryUpdateDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateSecretaryById_shouldThrowBusinessException_whenInactive() {
        Secretary secretary = new Secretary();
        secretary.setIsActive(false);

        when(secretaryRepository.findById(1L)).thenReturn(Optional.of(secretary));

        assertThatThrownBy(() -> service.updateSecretaryById(1L, secretaryUpdateDTO()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updateSecretaryById_shouldUpdateAndReturnDto_whenValid() {
        Secretary secretary = new Secretary();
        secretary.setIsActive(true);
        SecretaryUpdateDTO dto = secretaryUpdateDTO();
        SecretaryResponseDTO responseDTO = secretaryResponseDTO();
        PublicAgency publicAgency = new PublicAgency();

        when(secretaryRepository.findById(1L)).thenReturn(Optional.of(secretary));
        when(publicAgencyRepository.findById(dto.publicAgencyId())).thenReturn(Optional.of(publicAgency));
        when(secretaryRepository.save(any(Secretary.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(secretaryMapper.toDTO(any(Secretary.class))).thenReturn(responseDTO);

        SecretaryResponseDTO result = service.updateSecretaryById(1L, dto);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(secretary.getPublicAgency()).isEqualTo(publicAgency);
        assertThat(secretary.getCnpj()).isEqualTo("12345678000199");
        assertThat(secretary.getPhone()).isEqualTo("11988887777");
        assertThat(secretary.getContactPerson()).isEqualTo("11977776666");
        verify(secretaryRepository).save(secretary);
    }

    @Test
    void deleteSecretaryById_shouldThrowNotFound_whenMissing() {
        when(secretaryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteSecretaryById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteSecretaryById_shouldThrowBusinessException_whenInactive() {
        Secretary secretary = new Secretary();
        secretary.setIsActive(false);

        when(secretaryRepository.findById(1L)).thenReturn(Optional.of(secretary));

        assertThatThrownBy(() -> service.deleteSecretaryById(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deleteSecretaryById_shouldDeactivate_whenActive() {
        Secretary secretary = new Secretary();
        secretary.setIsActive(true);

        when(secretaryRepository.findById(1L)).thenReturn(Optional.of(secretary));
        when(secretaryRepository.save(any(Secretary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deleteSecretaryById(1L);

        assertThat(secretary.getIsActive()).isFalse();
        verify(secretaryRepository).save(secretary);
    }

    @Test
    void restoreSecretaryById_shouldThrowNotFound_whenMissing() {
        when(secretaryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restoreSecretaryById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void restoreSecretaryById_shouldThrowBusinessException_whenAlreadyActive() {
        Secretary secretary = new Secretary();
        secretary.setIsActive(true);

        when(secretaryRepository.findById(1L)).thenReturn(Optional.of(secretary));

        assertThatThrownBy(() -> service.restoreSecretaryById(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void restoreSecretaryById_shouldActivate_whenInactive() {
        Secretary secretary = new Secretary();
        secretary.setIsActive(false);
        SecretaryResponseDTO responseDTO = secretaryResponseDTO();

        when(secretaryRepository.findById(1L)).thenReturn(Optional.of(secretary));
        when(secretaryRepository.save(any(Secretary.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(secretaryMapper.toDTO(any(Secretary.class))).thenReturn(responseDTO);

        SecretaryResponseDTO result = service.restoreSecretaryById(1L);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(secretary.getIsActive()).isTrue();
    }

    @Test
    void activateSecretaryAsClientById_shouldThrowNotFound_whenMissing() {
        when(secretaryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activateSecretaryAsClientById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void activateSecretaryAsClientById_shouldThrowBusinessException_whenAlreadyClient() {
        Secretary secretary = new Secretary();
        secretary.setIsClient(true);

        when(secretaryRepository.findById(1L)).thenReturn(Optional.of(secretary));

        assertThatThrownBy(() -> service.activateSecretaryAsClientById(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void activateSecretaryAsClientById_shouldActivate_whenNotClient() {
        Secretary secretary = new Secretary();
        secretary.setIsClient(false);
        SecretaryResponseDTO responseDTO = secretaryResponseDTO();

        when(secretaryRepository.findById(1L)).thenReturn(Optional.of(secretary));
        when(secretaryRepository.save(any(Secretary.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(secretaryMapper.toDTO(any(Secretary.class))).thenReturn(responseDTO);

        SecretaryResponseDTO result = service.activateSecretaryAsClientById(1L);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(secretary.getIsClient()).isTrue();
    }

    @Test
    void desactivateSecretaryAsClientById_shouldThrowNotFound_whenMissing() {
        when(secretaryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desactivateSecretaryAsClientById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void desactivateSecretaryAsClientById_shouldThrowBusinessException_whenAlreadyNotClient() {
        Secretary secretary = new Secretary();
        secretary.setIsClient(false);

        when(secretaryRepository.findById(1L)).thenReturn(Optional.of(secretary));

        assertThatThrownBy(() -> service.desactivateSecretaryAsClientById(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void desactivateSecretaryAsClientById_shouldDeactivate_whenClient() {
        Secretary secretary = new Secretary();
        secretary.setIsClient(true);
        SecretaryResponseDTO responseDTO = secretaryResponseDTO();

        when(secretaryRepository.findById(1L)).thenReturn(Optional.of(secretary));
        when(secretaryRepository.save(any(Secretary.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(secretaryMapper.toDTO(any(Secretary.class))).thenReturn(responseDTO);

        SecretaryResponseDTO result = service.desactivateSecretaryAsClientById(1L);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(secretary.getIsClient()).isFalse();
    }

    private SecretaryRequestDTO secretaryRequestDTO() {
        return new SecretaryRequestDTO(
                "SEC01",
                "SEC",
                1L,
                "Secretaria Exemplo",
                "12.345.678/0001-99",
                true,
                "secretaria@gopro.com",
                "(11) 98888-7777",
                "Rua A, 10",
                "(11) 97777-6666",
                true,
                10L
        );
    }

    private SecretaryUpdateDTO secretaryUpdateDTO() {
        return new SecretaryUpdateDTO(
                "SEC02",
                "SEC",
                1L,
                "Secretaria Atualizada",
                "12.345.678/0001-99",
                true,
                "secretaria@gopro.com",
                "(11) 98888-7777",
                "Rua B, 20",
                "(11) 97777-6666",
                true,
                20L
        );
    }

    private SecretaryResponseDTO secretaryResponseDTO() {
        return new SecretaryResponseDTO(
                1L,
                "SEC01",
                "SEC",
                null,
                "Secretaria Exemplo",
                "12345678000199",
                true,
                "secretaria@gopro.com",
                "11988887777",
                "Rua A, 10",
                "11977776666",
                true,
                null,
                null,
                10L,
                null
        );
    }
}
