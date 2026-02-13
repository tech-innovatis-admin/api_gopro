package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PeopleRequestDTO;
import br.com.gopro.api.dtos.PeopleResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.PeopleMapper;
import br.com.gopro.api.model.People;
import br.com.gopro.api.repository.PeopleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleServiceImplTest {

    @Mock
    private PeopleRepository peopleRepository;

    @Mock
    private PeopleMapper peopleMapper;

    @InjectMocks
    private PeopleServiceImpl service;

    @Test
    void createPeople_shouldThrowBusinessException_whenCpfInvalid() {
        PeopleRequestDTO dto = peopleRequestDTO("111.111.111-11");

        when(peopleMapper.toEntity(dto)).thenReturn(new People());

        assertThatThrownBy(() -> service.createPeople(dto))
                .isInstanceOf(BusinessException.class);

        verify(peopleRepository, never()).save(any());
    }

    @Test
    void createPeople_shouldNormalizeFieldsAndReturnDto_whenCpfValid() {
        PeopleRequestDTO dto = peopleRequestDTO("529.982.247-25");
        PeopleResponseDTO responseDTO = peopleResponseDTO();
        People people = new People();

        when(peopleMapper.toEntity(dto)).thenReturn(people);
        when(peopleRepository.save(any(People.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(peopleMapper.toDTO(any(People.class))).thenReturn(responseDTO);

        PeopleResponseDTO result = service.createPeople(dto);

        assertThat(result).isEqualTo(responseDTO);

        ArgumentCaptor<People> captor = ArgumentCaptor.forClass(People.class);
        verify(peopleRepository).save(captor.capture());
        People saved = captor.getValue();

        assertThat(saved.getCpf()).isEqualTo("52998224725");
        assertThat(saved.getPhone()).isEqualTo("11988887777");
        assertThat(saved.getIsActive()).isTrue();
    }

    @Test
    void listAllPeoples_shouldReturnMappedList() {
        People people = new People();
        PeopleResponseDTO responseDTO = peopleResponseDTO();

        when(peopleRepository.findByIsActiveTrue(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(people), PageRequest.of(0, 10), 1));
        when(peopleMapper.toDTO(people)).thenReturn(responseDTO);

        PageResponseDTO<PeopleResponseDTO> result = service.listAllPeoples(0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0)).isEqualTo(responseDTO);
        verify(peopleRepository).findByIsActiveTrue(PageRequest.of(0, 10));
    }

    @Test
    void listPeopleById_shouldThrowNotFound_whenMissing() {
        when(peopleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listPeopleById(1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(peopleRepository).findById(1L);
        verifyNoInteractions(peopleMapper);
    }

    @Test
    void listPeopleById_shouldThrowNotFound_whenInactive() {
        People people = new People();
        people.setIsActive(false);

        when(peopleRepository.findById(1L)).thenReturn(Optional.of(people));

        assertThatThrownBy(() -> service.listPeopleById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listPeopleById_shouldReturnDto_whenFound() {
        People people = new People();
        people.setIsActive(true);
        PeopleResponseDTO responseDTO = peopleResponseDTO();

        when(peopleRepository.findById(1L)).thenReturn(Optional.of(people));
        when(peopleMapper.toDTO(people)).thenReturn(responseDTO);

        PeopleResponseDTO result = service.listPeopleById(1L);

        assertThat(result).isEqualTo(responseDTO);
        verify(peopleRepository).findById(1L);
        verify(peopleMapper).toDTO(people);
    }

    @Test
    void updatePeople_shouldThrowNotFound_whenMissing() {
        when(peopleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePeople(1L, peopleRequestDTO("529.982.247-25")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatePeople_shouldThrowBusinessException_whenInactive() {
        People people = new People();
        people.setIsActive(false);

        when(peopleRepository.findById(1L)).thenReturn(Optional.of(people));

        assertThatThrownBy(() -> service.updatePeople(1L, peopleRequestDTO("529.982.247-25")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updatePeople_shouldThrowBusinessException_whenCpfInvalid() {
        People people = new People();
        people.setIsActive(true);
        when(peopleRepository.findById(1L)).thenReturn(Optional.of(people));

        assertThatThrownBy(() -> service.updatePeople(1L, peopleRequestDTO("111.111.111-11")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updatePeople_shouldUpdateFieldsAndReturnDto() {
        People people = new People();
        people.setIsActive(true);
        PeopleRequestDTO dto = peopleRequestDTO("529.982.247-25");
        PeopleResponseDTO responseDTO = peopleResponseDTO();

        when(peopleRepository.findById(1L)).thenReturn(Optional.of(people));
        when(peopleRepository.save(any(People.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(peopleMapper.toDTO(any(People.class))).thenReturn(responseDTO);

        PeopleResponseDTO result = service.updatePeople(1L, dto);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(people.getCpf()).isEqualTo("52998224725");
        assertThat(people.getPhone()).isEqualTo("11988887777");
        assertThat(people.getFullName()).isEqualTo("Ana Silva");
        verify(peopleRepository).save(people);
    }

    @Test
    void deletePeople_shouldThrowNotFound_whenMissing() {
        when(peopleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletePeople(1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(peopleRepository).findById(1L);
        verify(peopleRepository, never()).save(any());
    }

    @Test
    void deletePeople_shouldThrowBusinessException_whenInactive() {
        People people = new People();
        people.setIsActive(false);

        when(peopleRepository.findById(1L)).thenReturn(Optional.of(people));

        assertThatThrownBy(() -> service.deletePeople(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deletePeople_shouldDelete_whenExists() {
        People people = new People();
        people.setIsActive(true);

        when(peopleRepository.findById(1L)).thenReturn(Optional.of(people));
        when(peopleRepository.save(any(People.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deletePeople(1L);

        assertThat(people.getIsActive()).isFalse();
        verify(peopleRepository).save(people);
    }

    @Test
    void restorePeople_shouldThrowNotFound_whenMissing() {
        when(peopleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restorePeople(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void restorePeople_shouldThrowBusinessException_whenAlreadyActive() {
        People people = new People();
        people.setIsActive(true);

        when(peopleRepository.findById(1L)).thenReturn(Optional.of(people));

        assertThatThrownBy(() -> service.restorePeople(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void restorePeople_shouldRestore_whenInactive() {
        People people = new People();
        people.setIsActive(false);
        PeopleResponseDTO responseDTO = peopleResponseDTO();

        when(peopleRepository.findById(1L)).thenReturn(Optional.of(people));
        when(peopleRepository.save(any(People.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(peopleMapper.toDTO(any(People.class))).thenReturn(responseDTO);

        PeopleResponseDTO result = service.restorePeople(1L);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(people.getIsActive()).isTrue();
    }

    private PeopleRequestDTO peopleRequestDTO(String cpf) {
        return new PeopleRequestDTO(
                "Ana Silva",
                cpf,
                "ana@gopro.com",
                "(11) 98888-7777",
                "https://bucket.s3.amazonaws.com/avatars/ana.png",
                LocalDate.of(1990, 1, 1),
                "Rua A, 10",
                "Sao Paulo",
                "SP",
                "Observacao"
        );
    }

    private PeopleResponseDTO peopleResponseDTO() {
        return new PeopleResponseDTO(
                1L,
                "Ana Silva",
                "52998224725",
                "ana@gopro.com",
                "11988887777",
                "https://bucket.s3.amazonaws.com/avatars/ana.png",
                LocalDate.of(1990, 1, 1),
                "Rua A, 10",
                "Sao Paulo",
                "SP",
                "Observacao",
                true
        );
    }
}
