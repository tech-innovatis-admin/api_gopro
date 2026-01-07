package br.com.gopro.api.repository;

import br.com.gopro.api.model.ProjectOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectOrganizationRepository extends JpaRepository<ProjectOrganization, Long> {
}
