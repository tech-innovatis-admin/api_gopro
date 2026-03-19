package br.com.gopro.api.repository;

import br.com.gopro.api.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Page<Expense> findAll(Pageable pageable);
    Page<Expense> findByIsActiveTrue(Pageable pageable);
    Page<Expense> findByIsActiveTrueAndProject_Id(Long projectId, Pageable pageable);

    @Query("""
    select coalesce(e.project.id, i.project.id)
    from Expense e
    left join e.income i
    where e.id = :id
""")
    Optional<Long> findProjectIdById(@Param("id") Long id);

    @Query("""
    select coalesce(sum(e.amount), 0)
    from Expense e
    where e.project.id = :projectId
""")
    BigDecimal sumExpenseByProjectId(@Param("projectId") Long projectId);
}
