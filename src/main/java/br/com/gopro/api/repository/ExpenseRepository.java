package br.com.gopro.api.repository;

import br.com.gopro.api.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Page<Expense> findAll(Pageable pageable);
    Page<Expense> findByIsActiveTrue(Pageable pageable);

    @Query("""
    select coalesce(sum(e.amount), 0)
    from Expense e
    join e.income i
    where i.project.id = :projectId
""")
    BigDecimal sumExpenseByProjectId(@Param("projectId") Long projectId);
}
