package br.com.gopro.api.repository;

import br.com.gopro.api.model.BudgetTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetTransferRepository extends JpaRepository<BudgetTransfer, Long> {
    Page<BudgetTransfer> findByIsActiveTrue(Pageable pageable);
    Page<BudgetTransfer> findByIsActiveTrueAndProject_Id(Long projectId, Pageable pageable);

    @Query("select t.project.id from BudgetTransfer t where t.id = :id")
    Optional<Long> findProjectIdById(@Param("id") Long id);
}
