package br.com.gopro.api.repository;

import br.com.gopro.api.model.BudgetItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetItemRepository extends JpaRepository<BudgetItems, Long> {
}
