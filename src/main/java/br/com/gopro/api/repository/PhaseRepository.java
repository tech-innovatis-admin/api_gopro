package br.com.gopro.api.repository;

import br.com.gopro.api.model.Phase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhaseRepository extends JpaRepository<Phase, Long> {
    Page<Phase> findByIsActiveTrue(Pageable pageable);
}