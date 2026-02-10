package br.com.gopro.api.repository;

import br.com.gopro.api.model.BudgetCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long> {
    Page<BudgetCategory> findByIsActiveTrue(Pageable pageable);
    Page<BudgetCategory> findByIsActiveTrueAndProject_Id(Long projectId, Pageable pageable);
}
