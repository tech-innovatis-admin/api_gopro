package br.com.gopro.api.repository;

import br.com.gopro.api.model.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    Page<Goal> findByIsActiveTrue(Pageable pageable);
    Page<Goal> findByIsActiveTrueAndProject_Id(Long projectId, Pageable pageable);

    @Query("select g.project.id from Goal g where g.id = :id")
    Optional<Long> findProjectIdById(@Param("id") Long id);
}
