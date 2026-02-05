package br.com.gopro.api.repository;

import br.com.gopro.api.model.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    Page<Partner> findByIsActiveTrue(Pageable pageable);
}
