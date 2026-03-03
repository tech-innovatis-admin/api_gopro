package br.com.gopro.api.repository;

import br.com.gopro.api.model.Phase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhaseRepository extends JpaRepository<Phase, Long> {
    Page<Phase> findByIsActiveTrue(Pageable pageable);
    Page<Phase> findByIsActiveTrueAndStage_Id(Long stageId, Pageable pageable);
    Page<Phase> findByIsActiveTrueAndStage_Goal_Project_Id(Long projectId, Pageable pageable);

    @Query("select p.stage.goal.project.id from Phase p where p.id = :id")
    Optional<Long> findProjectIdById(@Param("id") Long id);
}
