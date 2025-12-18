package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PeopleRequestDTO;
import br.com.gopro.api.dtos.PeopleResponseDTO;

import java.util.List;

public interface PeopleService {
    PeopleResponseDTO createPeople(PeopleRequestDTO dto);
    List<PeopleResponseDTO> listAllPeoples();
    PeopleResponseDTO listPeopleById(Long id);
    PeopleResponseDTO updatePeople(Long id, PeopleRequestDTO dto);
    void deletePeople(Long id);
}
