package br.com.gopro.api.repository;

import br.com.gopro.api.model.BudgetCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetCategoriesRepository extends JpaRepository<BudgetCategories, Long> {
}
