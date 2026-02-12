package br.com.gopro.api.repository;

import br.com.gopro.api.dtos.ProjectPeopleDetailedResponseDTO;
import br.com.gopro.api.model.ProjectPeople;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectPeopleRepository extends JpaRepository<ProjectPeople, Long> {
    Page<ProjectPeople> findByIsActiveTrue(Pageable pageable);
    Page<ProjectPeople> findByProject_IdAndIsActiveTrue(Long projectId, Pageable pageable);

    @Query(
            value = """
                    select new br.com.gopro.api.dtos.ProjectPeopleDetailedResponseDTO(
                        pp.id,
                        p.id,
                        pe.id,
                        pp.role,
                        pp.workloadHours,
                        pp.institutionalLink,
                        pp.contractType,
                        pp.startDate,
                        pp.endDate,
                        pp.status,
                        pp.baseAmount,
                        pp.notes,
                        pp.isActive,
                        pp.createdAt,
                        pp.updatedAt,
                        pp.createdBy,
                        pp.updatedBy,
                        pe.fullName,
                        pe.cpf,
                        pe.email,
                        pe.phone,
                        pe.avatarUrl,
                        pe.birthDate,
                        pe.address,
                        pe.zipCode,
                        pe.city,
                        pe.state
                    )
                    from ProjectPeople pp
                    join pp.project p
                    join pp.person pe
                    where pp.isActive = true
                      and (:projectId is null or p.id = :projectId)
                    """,
            countQuery = """
                    select count(pp.id)
                    from ProjectPeople pp
                    join pp.project p
                    where pp.isActive = true
                      and (:projectId is null or p.id = :projectId)
                    """
    )
    Page<ProjectPeopleDetailedResponseDTO> findDetailedByProjectId(
            @Param("projectId") Long projectId,
            Pageable pageable
    );
}
