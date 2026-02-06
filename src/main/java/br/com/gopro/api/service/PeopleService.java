package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PeopleRequestDTO;
import br.com.gopro.api.dtos.PeopleResponseDTO;

public interface PeopleService {
    PeopleResponseDTO createPeople(PeopleRequestDTO dto);
    PageResponseDTO<PeopleResponseDTO> listAllPeoples(int page, int size);
    PeopleResponseDTO listPeopleById(Long id);
    PeopleResponseDTO updatePeople(Long id, PeopleRequestDTO dto);
    void deletePeople(Long id);
    PeopleResponseDTO restorePeople(Long id);
}