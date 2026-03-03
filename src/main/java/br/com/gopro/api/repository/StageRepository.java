package br.com.gopro.api.repository;

import br.com.gopro.api.model.Stage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {
    Page<Stage> findByIsActiveTrue(Pageable pageable);
    Page<Stage> findByIsActiveTrueAndGoal_Id(Long goalId, Pageable pageable);
    Page<Stage> findByIsActiveTrueAndGoal_Project_Id(Long projectId, Pageable pageable);

    @Query("select s.goal.project.id from Stage s where s.id = :id")
    Optional<Long> findProjectIdById(@Param("id") Long id);
}
