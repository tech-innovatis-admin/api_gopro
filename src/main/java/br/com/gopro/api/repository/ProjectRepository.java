package br.com.gopro.api.repository;

import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import br.com.gopro.api.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByCode(String code);
    Page<Project> findAll(Pageable pageable);
    Page<Project> findByIsActiveTrue(Pageable pageable);
    List<Project> findByIsActiveTrue();
    Optional<Project> findByCode(String code);

    @Query("""
    select
        p.id as id,
        p.name as name,
        p.code as code,
        cast(p.projectStatus as string) as projectStatus,
        coalesce(sum(distinct i.amount), 0) as totalReceived,
        coalesce(sum(e.amount), 0) as totalExpenses,
        (coalesce(sum(distinct i.amount), 0) - coalesce(sum(e.amount), 0)) as saldo
    from Project p
    left join Income i on i.project.id = p.id
    left join Expense e on e.income.id = i.id
    group by p.id, p.name, p.code, p.projectStatus
    order by p.createdAt desc
""")
    List<ProjectTotalRow> findAllWithTotals();

    @Query("""
    select
        p.projectType as projectType,
        count(p) as contracts,
        coalesce(sum(p.contractValue), 0) as totalValue
    from Project p
    where p.isActive = true
      and (:projectStatus is null or p.projectStatus = :projectStatus)
    group by p.projectType
""")
    List<ProjectTypeSummaryProjection> aggregateTypeDistribution(@Param("projectStatus") ProjectStatusEnum projectStatus);

    @Query("""
    select
        function('month', coalesce(p.startDate, p.openingDate)) as month,
        count(p) as contracts,
        coalesce(sum(p.contractValue), 0) as totalValue
    from Project p
    where p.isActive = true
      and coalesce(p.startDate, p.openingDate) is not null
    group by function('month', coalesce(p.startDate, p.openingDate))
""")
    List<ProjectMonthSummaryProjection> aggregateByMonth();

    @Query("""
    select
        p.city as city,
        p.state as state,
        p.executionLocation as executionLocation,
        count(p) as contracts,
        coalesce(sum(p.contractValue), 0) as totalValue
    from Project p
    where p.isActive = true
      and (
          lower(coalesce(p.city, '')) like concat('%', :location, '%')
          or lower(coalesce(p.state, '')) like concat('%', :location, '%')
          or lower(coalesce(p.executionLocation, '')) like concat('%', :location, '%')
      )
    group by p.city, p.state, p.executionLocation
""")
    List<ProjectLocationSummaryProjection> aggregateByLocation(@Param("location") String location);

    @Query("""
    select
        pp.id as partnerId,
        pp.name as partnerName,
        count(p) as contracts,
        coalesce(sum(p.contractValue), 0) as totalValue
    from Project p
    left join p.primaryPartner pp
    where p.isActive = true
    group by pp.id, pp.name
""")
    List<ProjectPartnerSummaryProjection> aggregateByPrimaryPartner();

    @Query("""
    select
        count(p) as contracts,
        coalesce(sum(p.contractValue), 0) as totalValue
    from Project p
    where p.isActive = true
      and (p.primaryPartner.id = :partnerId or p.secundaryPartner.id = :partnerId)
""")
    ProjectTotalsSummaryProjection aggregateTotalsByPartner(@Param("partnerId") Long partnerId);

    interface ProjectTypeSummaryProjection {
        ProjectTypeEnum getProjectType();
        Long getContracts();
        BigDecimal getTotalValue();
    }

    interface ProjectMonthSummaryProjection {
        Integer getMonth();
        Long getContracts();
        BigDecimal getTotalValue();
    }

    interface ProjectLocationSummaryProjection {
        String getCity();
        String getState();
        String getExecutionLocation();
        Long getContracts();
        BigDecimal getTotalValue();
    }

    interface ProjectPartnerSummaryProjection {
        Long getPartnerId();
        String getPartnerName();
        Long getContracts();
        BigDecimal getTotalValue();
    }

    interface ProjectTotalsSummaryProjection {
        Long getContracts();
        BigDecimal getTotalValue();
    }

}
