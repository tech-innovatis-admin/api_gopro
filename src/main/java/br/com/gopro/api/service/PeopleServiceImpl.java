package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PeopleRequestDTO;
import br.com.gopro.api.dtos.PeopleResponseDTO;
import br.com.gopro.api.mapper.PeopleMapper;
import br.com.gopro.api.model.People;
import br.com.gopro.api.repository.PeopleRepository;
import br.com.gopro.api.utils.NormalizeUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PeopleServiceImpl implements PeopleService{

    private final PeopleRepository peopleRepository;
    private final PeopleMapper peopleMapper;

    @Override
    public PeopleResponseDTO createPeople(PeopleRequestDTO dto) {
        People people = peopleMapper.toEntity(dto);

        String cpfNormalizado = NormalizeUtils.normalizeCpf(dto.cpf());

        if (!NormalizeUtils.isValidCpf(cpfNormalizado)) {
            throw new IllegalArgumentException("CPF é inválido!");
        }

        people.setCpf(cpfNormalizado);
        people.setPhone(NormalizeUtils.normalizePhone(dto.phone()));
        people.setZipCode(NormalizeUtils.normalizeZipCode(dto.zipCode()));

        People peopleSave = peopleRepository.save(people);

        return peopleMapper.toDTO(peopleSave);
    }

    @Override
    public List<PeopleResponseDTO> listAllPeoples() {
        return peopleRepository.findAll().stream()
                .map(peopleMapper::toDTO)
                .toList();
    }

    @Override
    public PeopleResponseDTO listPeopleById(Long id) {
        People people = peopleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pessoa não encontrada!"));

        return peopleMapper.toDTO(people);
    }

    @Transactional
    @Override
    public PeopleResponseDTO updatePeople(Long id, PeopleRequestDTO dto) {
        People people = peopleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pessoa não encontrada!"));

        String cpfNormalizado = NormalizeUtils.normalizeCpf(dto.cpf());

        if (!NormalizeUtils.isValidCpf(cpfNormalizado)) {
            throw new IllegalArgumentException("CPF é inválido!");
        }

        people.setFullName(dto.fullName());
        people.setCpf(cpfNormalizado);
        people.setEmail(dto.email());
        people.setPhone(NormalizeUtils.normalizePhone(dto.phone()));
        people.setBirthDate(dto.birthDate());
        people.setAddress(dto.address());
        people.setZipCode(NormalizeUtils.normalizeZipCode(dto.zipCode()));
        people.setCity(dto.city());
        people.setState(dto.state());
        people.setNotes(dto.notes());

        People peopleUpdated = peopleRepository.save(people);

        return peopleMapper.toDTO(peopleUpdated);

    }

    @Transactional
    @Override
    public void deletePeople(Long id) {
        if (!peopleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Pessoa não encontrada!");
        }

        peopleRepository.deleteById(id);
    }
}
