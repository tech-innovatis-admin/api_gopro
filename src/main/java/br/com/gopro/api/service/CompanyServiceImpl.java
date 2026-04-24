package br.com.gopro.api.service;

import br.com.gopro.api.dtos.CompanyRequestDTO;
import br.com.gopro.api.dtos.CompanyResponseDTO;
import br.com.gopro.api.dtos.CompanyUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.CompanyMapper;
import br.com.gopro.api.model.Company;
import br.com.gopro.api.model.People;
import br.com.gopro.api.repository.CompanyRepository;
import br.com.gopro.api.repository.PeopleRepository;
import br.com.gopro.api.utils.NormalizeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final PeopleRepository peopleRepository;

    @Override
    public CompanyResponseDTO createCompany(CompanyRequestDTO dto) {
        Company company = companyMapper.toEntity(dto);
        company.setCnpj(normalizeCnpj(dto.cnpj()));
        company.setResponsiblePerson(resolveResponsiblePerson(dto.responsiblePersonId()));
        ensureUniqueCnpj(company.getCnpj(), null);
        company.setIsActive(true);
        Company saved = companyRepository.save(company);
        return companyMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<CompanyResponseDTO> listAllCompanies(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Company> pageResult = companyRepository.findByIsActiveTrue(pageable);
        List<CompanyResponseDTO> content = pageResult.getContent().stream()
                .map(companyMapper::toDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isFirst(),
                pageResult.isLast()
        );
    }

    @Override
    public CompanyResponseDTO findCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
        if (!Boolean.TRUE.equals(company.getIsActive())) {
            throw new ResourceNotFoundException("Empresa nao encontrada");
        }
        return companyMapper.toDTO(company);
    }

    @Override
    public CompanyResponseDTO updateCompanyById(Long id, CompanyUpdateDTO dto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
        if (!Boolean.TRUE.equals(company.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar uma empresa inativa");
        }
        companyMapper.updateEntityFromDTO(dto, company);
        if (dto.cnpj() != null) {
            company.setCnpj(normalizeCnpj(dto.cnpj()));
        }
        company.setResponsiblePerson(resolveResponsiblePerson(dto.responsiblePersonId()));
        ensureUniqueCnpj(company.getCnpj(), company.getId());
        Company updated = companyRepository.save(company);
        return companyMapper.toDTO(updated);
    }

    @Override
    public void deleteCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
        if (!Boolean.TRUE.equals(company.getIsActive())) {
            throw new BusinessException("Empresa ja esta inativa");
        }
        company.setIsActive(false);
        companyRepository.save(company);
    }

    @Override
    public CompanyResponseDTO restoreCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
        if (Boolean.TRUE.equals(company.getIsActive())) {
            throw new BusinessException("Empresa ja esta ativa");
        }
        company.setIsActive(true);
        Company restored = companyRepository.save(company);
        return companyMapper.toDTO(restored);
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }
    }

    private void ensureUniqueCnpj(String cnpj, Long currentCompanyId) {
        companyRepository.findByCnpj(cnpj)
                .filter(company -> currentCompanyId == null || !company.getId().equals(currentCompanyId))
                .ifPresent(company -> {
                    throw new BusinessException("Empresa ja existe para este CNPJ");
                });
    }

    private String normalizeCnpj(String cnpj) {
        String normalized = NormalizeUtils.normalizeCnpj(cnpj);
        if (normalized == null || normalized.isBlank()) {
            throw new BusinessException("CNPJ e obrigatorio");
        }
        return normalized;
    }

    private People resolveResponsiblePerson(Long responsiblePersonId) {
        if (responsiblePersonId == null) {
            return null;
        }

        People responsiblePerson = peopleRepository.findById(responsiblePersonId)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa responsavel nao encontrada"));

        if (!Boolean.TRUE.equals(responsiblePerson.getIsActive())) {
            throw new BusinessException("Pessoa responsavel inativa");
        }

        return responsiblePerson;
    }
}
