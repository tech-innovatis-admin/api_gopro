package br.com.gopro.api.repository;

import br.com.gopro.api.model.ProjectPeople;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectPeopleRepository extends JpaRepository<ProjectPeople, Long> {
    Page<ProjectPeople> findByIsActiveTrue(Pageable pageable);
}