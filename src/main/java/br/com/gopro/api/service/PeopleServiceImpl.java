package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PeopleRequestDTO;
import br.com.gopro.api.dtos.PeopleResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.PeopleMapper;
import br.com.gopro.api.model.People;
import br.com.gopro.api.repository.PeopleRepository;
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
public class PeopleServiceImpl implements PeopleService {

    private final PeopleRepository peopleRepository;
    private final PeopleMapper peopleMapper;

    @Override
    public PeopleResponseDTO createPeople(PeopleRequestDTO dto) {
        People people = peopleMapper.toEntity(dto);

        String cpfNormalizado = NormalizeUtils.normalizeCpf(dto.cpf());
        String zipCodeNormalizado = normalizeRequiredZipCode(dto.zipCode());
        String cityNormalizada = normalizeRequiredText(dto.city(), "Cidade e obrigatoria");
        String stateNormalizado = normalizeRequiredText(dto.state(), "Estado e obrigatorio").toUpperCase();

        if (!NormalizeUtils.isValidCpf(cpfNormalizado)) {
            throw new BusinessException("CPF e invalido");
        }
        if (dto.birthDate() == null) {
            throw new BusinessException("Data de nascimento e obrigatoria");
        }

        people.setFullName(NormalizeUtils.normalizeOrNull(dto.fullName()));
        people.setCpf(cpfNormalizado);
        people.setEmail(NormalizeUtils.normalizeOrNull(dto.email()));
        people.setPhone(NormalizeUtils.normalizePhone(dto.phone()));
        people.setAvatarUrl(dto.avatarUrl());
        people.setBirthDate(dto.birthDate());
        people.setAddress(NormalizeUtils.normalizeOrNull(dto.address()));
        people.setZipCode(zipCodeNormalizado);
        people.setCity(cityNormalizada);
        people.setState(stateNormalizado);
        people.setNotes(NormalizeUtils.normalizeOrNull(dto.notes()));
        people.setIsActive(true);

        People peopleSave = peopleRepository.save(people);

        return peopleMapper.toDTO(peopleSave);
    }

    @Override
    public PageResponseDTO<PeopleResponseDTO> listAllPeoples(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<People> peoplePage = peopleRepository.findByIsActiveTrue(pageable);
        List<PeopleResponseDTO> content = peoplePage.getContent().stream()
                .map(peopleMapper::toDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                peoplePage.getNumber(),
                peoplePage.getSize(),
                peoplePage.getTotalElements(),
                peoplePage.getTotalPages(),
                peoplePage.isFirst(),
                peoplePage.isLast()
        );
    }

    @Override
    public PeopleResponseDTO listPeopleById(Long id) {
        People people = peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa nao encontrada"));

        if (!Boolean.TRUE.equals(people.getIsActive())) {
            throw new ResourceNotFoundException("Pessoa nao encontrada");
        }

        return peopleMapper.toDTO(people);
    }

    @Transactional
    @Override
    public PeopleResponseDTO updatePeople(Long id, PeopleRequestDTO dto) {
        People people = peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa nao encontrada"));

        if (!Boolean.TRUE.equals(people.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar uma pessoa inativa");
        }

        String cpfNormalizado = NormalizeUtils.normalizeCpf(dto.cpf());
        String zipCodeNormalizado = normalizeRequiredZipCode(dto.zipCode());
        String cityNormalizada = normalizeRequiredText(dto.city(), "Cidade e obrigatoria");
        String stateNormalizado = normalizeRequiredText(dto.state(), "Estado e obrigatorio").toUpperCase();

        if (!NormalizeUtils.isValidCpf(cpfNormalizado)) {
            throw new BusinessException("CPF e invalido");
        }
        if (dto.birthDate() == null) {
            throw new BusinessException("Data de nascimento e obrigatoria");
        }

        people.setFullName(NormalizeUtils.normalizeOrNull(dto.fullName()));
        people.setCpf(cpfNormalizado);
        people.setEmail(NormalizeUtils.normalizeOrNull(dto.email()));
        people.setPhone(NormalizeUtils.normalizePhone(dto.phone()));
        people.setAvatarUrl(dto.avatarUrl());
        people.setBirthDate(dto.birthDate());
        people.setAddress(NormalizeUtils.normalizeOrNull(dto.address()));
        people.setZipCode(zipCodeNormalizado);
        people.setCity(cityNormalizada);
        people.setState(stateNormalizado);
        people.setNotes(NormalizeUtils.normalizeOrNull(dto.notes()));

        People peopleUpdated = peopleRepository.save(people);

        return peopleMapper.toDTO(peopleUpdated);
    }

    @Transactional
    @Override
    public void deletePeople(Long id) {
        People people = peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa nao encontrada"));

        if (!Boolean.TRUE.equals(people.getIsActive())) {
            throw new BusinessException("Pessoa ja esta inativa");
        }

        people.setIsActive(false);
        peopleRepository.save(people);
    }

    @Transactional
    @Override
    public PeopleResponseDTO restorePeople(Long id) {
        People people = peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa nao encontrada"));

        if (Boolean.TRUE.equals(people.getIsActive())) {
            throw new BusinessException("Pessoa ja esta ativa");
        }

        people.setIsActive(true);
        People restored = peopleRepository.save(people);
        return peopleMapper.toDTO(restored);
    }

    private String normalizeRequiredText(String value, String message) {
        String normalized = NormalizeUtils.normalizeOrNull(value);
        if (normalized == null) {
            throw new BusinessException(message);
        }
        return normalized;
    }

    private String normalizeRequiredZipCode(String value) {
        String normalized = NormalizeUtils.normalizeZipCode(value);
        if (normalized == null || normalized.length() != 8) {
            throw new BusinessException("CEP deve conter 8 digitos");
        }
        return normalized;
    }
}
