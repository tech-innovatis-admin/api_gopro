package br.com.gopro.api.repository;

import br.com.gopro.api.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Page<Company> findByIsActiveTrue(Pageable pageable);
    Optional<Company> findByCnpj(String cnpj);
}
