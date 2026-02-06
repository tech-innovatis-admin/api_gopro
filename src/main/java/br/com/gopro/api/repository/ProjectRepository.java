package br.com.gopro.api.repository;

import br.com.gopro.api.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByCode(String code);
    Page<Project> findAll(Pageable pageable);
    Page<Project> findByIsActiveTrue(Pageable pageable);
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


}
