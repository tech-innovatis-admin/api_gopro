package br.com.gopro.api.repository;

import br.com.gopro.api.model.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    Page<Income> findAll(Pageable pageable);
    Page<Income> findByIsActiveTrue(Pageable pageable);
    Page<Income> findByIsActiveTrueAndProject_Id(Long projectId, Pageable pageable);

    @Query("select i.project.id from Income i where i.id = :id")
    Optional<Long> findProjectIdById(@Param("id") Long id);

    @Query("""
    select coalesce(sum(i.amount), 0)
    from Income i
    where i.project.id = :projectId
""")
    BigDecimal sumIncomeByProjectId(@Param("projectId") Long projectId);
}
