package br.com.gopro.api.repository;

import br.com.gopro.api.model.Secretary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecretaryRepository extends JpaRepository<Secretary, Long> {
    @EntityGraph(attributePaths = "publicAgency")
    Page<Secretary> findByIsActiveTrue(Pageable pageable);
}
