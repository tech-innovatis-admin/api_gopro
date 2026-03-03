package br.com.gopro.api.repository;

import br.com.gopro.api.model.DisbursementSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisbursementScheduleRepository extends JpaRepository<DisbursementSchedule, Long> {
    Page<DisbursementSchedule> findByIsActiveTrue(Pageable pageable);
    Page<DisbursementSchedule> findByIsActiveTrueAndProject_Id(Long projectId, Pageable pageable);

    @Query("select s.project.id from DisbursementSchedule s where s.id = :id")
    Optional<Long> findProjectIdById(@Param("id") Long id);
}
