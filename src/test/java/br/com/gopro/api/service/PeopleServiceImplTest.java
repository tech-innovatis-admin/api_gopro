package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PeopleRequestDTO;
import br.com.gopro.api.dtos.PeopleResponseDTO;
import br.com.gopro.api.mapper.PeopleMapper;
import br.com.gopro.api.model.People;
import br.com.gopro.api.repository.PeopleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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
    void createPeople_shouldThrowIllegalArgumentException_whenCpfInvalid() {
        PeopleRequestDTO dto = peopleRequestDTO("111.111.111-11");

        when(peopleMapper.toEntity(dto)).thenReturn(new People());

        assertThatThrownBy(() -> service.createPeople(dto))
                .isInstanceOf(IllegalArgumentException.class);

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
        assertThat(saved.getZipCode()).isEqualTo("01310200");
    }

    @Test
    void listAllPeoples_shouldReturnMappedList() {
        People people = new People();
        PeopleResponseDTO responseDTO = peopleResponseDTO();

        when(peopleRepository.findAll()).thenReturn(List.of(people));
        when(peopleMapper.toDTO(people)).thenReturn(responseDTO);

        List<PeopleResponseDTO> result = service.listAllPeoples();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(responseDTO);
        verify(peopleRepository).findAll();
    }

    @Test
    void listPeopleById_shouldThrowNotFound_whenMissing() {
        when(peopleRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> service.listPeopleById(1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(peopleRepository).findById(1L);
        verifyNoInteractions(peopleMapper);
    }

    @Test
    void listPeopleById_shouldReturnDto_whenFound() {
        People people = new People();
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

        ResponseStatusException ex = catchThrowableOfType(
                () -> service.updatePeople(1L, peopleRequestDTO("529.982.247-25")),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updatePeople_shouldThrowIllegalArgumentException_whenCpfInvalid() {
        People people = new People();
        when(peopleRepository.findById(1L)).thenReturn(Optional.of(people));

        assertThatThrownBy(() -> service.updatePeople(1L, peopleRequestDTO("111.111.111-11")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updatePeople_shouldUpdateFieldsAndReturnDto() {
        People people = new People();
        PeopleRequestDTO dto = peopleRequestDTO("529.982.247-25");
        PeopleResponseDTO responseDTO = peopleResponseDTO();

        when(peopleRepository.findById(1L)).thenReturn(Optional.of(people));
        when(peopleRepository.save(any(People.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(peopleMapper.toDTO(any(People.class))).thenReturn(responseDTO);

        PeopleResponseDTO result = service.updatePeople(1L, dto);

        assertThat(result).isEqualTo(responseDTO);
        assertThat(people.getCpf()).isEqualTo("52998224725");
        assertThat(people.getPhone()).isEqualTo("11988887777");
        assertThat(people.getZipCode()).isEqualTo("01310200");
        assertThat(people.getFullName()).isEqualTo("Ana Silva");
        verify(peopleRepository).save(people);
    }

    @Test
    void deletePeople_shouldThrowNotFound_whenMissing() {
        when(peopleRepository.existsById(1L)).thenReturn(false);

        ResponseStatusException ex = catchThrowableOfType(
                () -> service.deletePeople(1L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(peopleRepository).existsById(1L);
        verify(peopleRepository, never()).deleteById(anyLong());
    }

    @Test
    void deletePeople_shouldDelete_whenExists() {
        when(peopleRepository.existsById(1L)).thenReturn(true);

        service.deletePeople(1L);

        verify(peopleRepository).deleteById(1L);
    }

    private PeopleRequestDTO peopleRequestDTO(String cpf) {
        return new PeopleRequestDTO(
                "Ana Silva",
                cpf,
                "ana@gopro.com",
                "(11) 98888-7777",
                LocalDate.of(1990, 1, 1),
                "Rua A, 10",
                "01310-200",
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
                LocalDate.of(1990, 1, 1),
                "Rua A, 10",
                "01310200",
                "Sao Paulo",
                "SP",
                "Observacao"
        );
    }
}
