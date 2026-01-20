package br.com.gopro.api.repository;

import br.com.gopro.api.model.DisbursementSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisbursementScheduleRepository extends JpaRepository<DisbursementSchedule, Long> {
}
