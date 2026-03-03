package br.com.gopro.api.service;

import br.com.gopro.api.dtos.UserLookupResponseDTO;

import java.util.List;

public interface UserDirectoryService {

    List<UserLookupResponseDTO> lookupByIds(List<Long> ids);
}

