package br.com.gopro.api.repository;

import br.com.gopro.api.model.BudgetItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetItemRepository extends JpaRepository<BudgetItem, Long> {
    Page<BudgetItem> findByIsActiveTrue(Pageable pageable);
    Page<BudgetItem> findByIsActiveTrueAndCategory_Id(Long categoryId, Pageable pageable);
    Page<BudgetItem> findByIsActiveTrueAndCategory_Project_Id(Long projectId, Pageable pageable);

    @Query("select i.category.project.id from BudgetItem i where i.id = :id")
    Optional<Long> findProjectIdById(@Param("id") Long id);

    List<BudgetItem> findByCategory_Project_IdAndProjectPeople_IdAndIsActiveTrue(Long projectId, Long projectPeopleId);

    List<BudgetItem> findByCategory_Project_IdAndProjectCompany_IdAndIsActiveTrue(Long projectId, Long projectCompanyId);

    @Query("""
        select coalesce(sum(i.contractedAmount), 0)
        from BudgetItem i
        where i.category.project.id = :projectId
          and i.projectPeople.id = :projectPeopleId
          and i.isActive = true
    """)
    BigDecimal sumContractedAmountByProjectAndProjectPeople(
            @Param("projectId") Long projectId,
            @Param("projectPeopleId") Long projectPeopleId
    );

    @Query("""
        select coalesce(sum(i.contractedAmount), 0)
        from BudgetItem i
        where i.category.project.id = :projectId
          and i.projectCompany.id = :projectCompanyId
          and i.isActive = true
    """)
    BigDecimal sumContractedAmountByProjectAndProjectCompany(
            @Param("projectId") Long projectId,
            @Param("projectCompanyId") Long projectCompanyId
    );
}
