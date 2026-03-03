package br.com.gopro.api.repository;

import br.com.gopro.api.model.BudgetCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long> {
    Page<BudgetCategory> findByIsActiveTrue(Pageable pageable);
    Page<BudgetCategory> findByIsActiveTrueAndProject_Id(Long projectId, Pageable pageable);

    @Query("select c.project.id from BudgetCategory c where c.id = :id")
    Optional<Long> findProjectIdById(@Param("id") Long id);
}
