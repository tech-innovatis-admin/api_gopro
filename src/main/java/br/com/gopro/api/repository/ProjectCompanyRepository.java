package br.com.gopro.api.repository;

import br.com.gopro.api.dtos.ProjectCompanyDetailedResponseDTO;
import br.com.gopro.api.model.ProjectCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectCompanyRepository extends JpaRepository<ProjectCompany, Long> {
    Page<ProjectCompany> findByIsActiveTrue(Pageable pageable);
    Page<ProjectCompany> findByProject_IdAndIsActiveTrue(Long projectId, Pageable pageable);

    @Query("select pc.project.id from ProjectCompany pc where pc.id = :id")
    Optional<Long> findProjectIdById(@Param("id") Long id);

    @Query(value = "select nextval('project_company_contract_number_seq')", nativeQuery = true)
    Long nextContractNumberSequence();

    @Query(
            value = """
                    select new br.com.gopro.api.dtos.ProjectCompanyDetailedResponseDTO(
                        pc.id,
                        p.id,
                        c.id,
                        pc.contractNumber,
                        pc.description,
                        pc.startDate,
                        pc.endDate,
                        pc.status,
                        pc.totalValue,
                        pc.notes,
                        pc.isIncubated,
                        pc.serviceType,
                        pc.isActive,
                        pc.createdAt,
                        pc.updatedAt,
                        pc.createdBy,
                        pc.updatedBy,
                        c.name,
                        c.tradeName,
                        c.cnpj,
                        c.email,
                        c.phone,
                        c.address,
                        c.city,
                        c.state
                    )
                    from ProjectCompany pc
                    join pc.project p
                    join pc.company c
                    where pc.isActive = true
                      and (:projectId is null or p.id = :projectId)
                    """,
            countQuery = """
                    select count(pc.id)
                    from ProjectCompany pc
                    join pc.project p
                    where pc.isActive = true
                      and (:projectId is null or p.id = :projectId)
                    """
    )
    Page<ProjectCompanyDetailedResponseDTO> findDetailedByProjectId(
            @Param("projectId") Long projectId,
            Pageable pageable
    );
}
