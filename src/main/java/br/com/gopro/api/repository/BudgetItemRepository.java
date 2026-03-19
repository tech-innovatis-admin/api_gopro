package br.com.gopro.api.repository;

import br.com.gopro.api.model.BudgetItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetItemRepository extends JpaRepository<BudgetItem, Long> {
    Page<BudgetItem> findByIsActiveTrue(Pageable pageable);
    Page<BudgetItem> findByIsActiveTrueAndCategory_Id(Long categoryId, Pageable pageable);
    Page<BudgetItem> findByIsActiveTrueAndCategory_Project_Id(Long projectId, Pageable pageable);

    @Query("select i.category.project.id from BudgetItem i where i.id = :id")
    Optional<Long> findProjectIdById(@Param("id") Long id);
}
