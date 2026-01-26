package br.com.gopro.api.repository;

import br.com.gopro.api.model.BudgetTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetTransferRepository extends JpaRepository<BudgetTransfer, Long> {
}
