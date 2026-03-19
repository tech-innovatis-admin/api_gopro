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
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    select p.code
    from Project p
    where p.code like concat(:prefix, '%')
""")
    List<String> findCodesByPrefix(@Param("prefix") String prefix);

    @Query("""
    select p
    from Project p
    where p.isActive = true
      and p.createdAt is not null
      and p.createdAt >= :fromDate
    order by p.createdAt desc, p.id desc
""")
    List<Project> findRecentCreatedProjects(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

    @Query("""
    select p
    from Project p
    where p.isActive = true
      and p.endDate is not null
      and p.endDate between :startDate and :endDate
    order by p.endDate asc, p.id asc
""")
    List<Project> findExpiringProjectsBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query(value = """
    select
        p.id as id,
        p.name as name,
        p.code as code,
        cast(p.project_status as varchar) as projectStatus,
        coalesce((
            select sum(i.amount)
            from incomes i
            where i.project_id = p.id
              and i.is_active = true
        ), 0) as totalReceived,
        coalesce((
            select sum(e.amount)
            from expenses e
            where e.project_id = p.id
              and e.is_active = true
        ), 0) as totalExpenses,
        (
            coalesce((
                select sum(i.amount)
                from incomes i
                where i.project_id = p.id
                  and i.is_active = true
            ), 0)
            -
            coalesce((
                select sum(e.amount)
                from expenses e
                where e.project_id = p.id
                  and e.is_active = true
            ), 0)
        ) as saldo
    from projects p
    where p.is_active = true
    order by p.created_at desc
""", nativeQuery = true)
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

    @Query(value = """
    select
        cast(extract(month from coalesce(p.start_date, p.opening_date)) as integer) as month,
        count(p.id) as contracts,
        coalesce(sum(p.contract_value), 0) as "totalValue"
    from projects p
    where p.is_active = true
      and coalesce(p.start_date, p.opening_date) is not null
      and cast(extract(year from coalesce(p.start_date, p.opening_date)) as integer) = :year
    group by cast(extract(month from coalesce(p.start_date, p.opening_date)) as integer)
""", nativeQuery = true)
    List<ProjectMonthSummaryProjection> aggregateByMonth(@Param("year") Integer year);

    @Query(value = """
    select distinct
        cast(extract(year from coalesce(p.start_date, p.opening_date)) as integer)
    from projects p
    where p.is_active = true
      and coalesce(p.start_date, p.opening_date) is not null
    order by cast(extract(year from coalesce(p.start_date, p.opening_date)) as integer) desc
""", nativeQuery = true)
    List<Integer> findAvailableYearsForMonthAnalytics();

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
