package br.com.gopro.api.repository;

import br.com.gopro.api.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Long, Organization> {
}
