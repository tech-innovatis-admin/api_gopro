package br.com.gopro.api.repository;

import br.com.gopro.api.model.Stage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {
    Page<Stage> findByIsActiveTrue(Pageable pageable);
    Page<Stage> findByIsActiveTrueAndGoal_Id(Long goalId, Pageable pageable);
    Page<Stage> findByIsActiveTrueAndGoal_Project_Id(Long projectId, Pageable pageable);
}
