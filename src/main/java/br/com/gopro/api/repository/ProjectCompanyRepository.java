package br.com.gopro.api.repository;

import br.com.gopro.api.model.ProjectCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectCompanyRepository extends JpaRepository<ProjectCompany, Long> {
    Page<ProjectCompany> findByIsActiveTrue(Pageable pageable);
}