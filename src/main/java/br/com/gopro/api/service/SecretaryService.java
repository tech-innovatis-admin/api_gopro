package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.SecretaryRequestDTO;
import br.com.gopro.api.dtos.SecretaryResponseDTO;
import br.com.gopro.api.dtos.SecretaryUpdateDTO;

public interface SecretaryService {
    SecretaryResponseDTO createSecretary(SecretaryRequestDTO dto);
    PageResponseDTO<SecretaryResponseDTO> listAllSecretary(int page, int size);
    SecretaryResponseDTO findSecretaryById(Long id);
    SecretaryResponseDTO updateSecretaryById(Long id, SecretaryUpdateDTO dto);
    void deleteSecretaryById(Long id);
    SecretaryResponseDTO restoreSecretaryById(Long id);
    SecretaryResponseDTO activateSecretaryAsClientById(Long id);
    SecretaryResponseDTO desactivateSecretaryAsClientById(Long id);
}
