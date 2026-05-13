package br.com.gopro.api.repository;

import br.com.gopro.api.model.BudgetItem;
import br.com.gopro.api.repository.projection.BeneficiaryBudgetSummaryProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@org.springframework.stereotype.Repository
public interface BeneficiaryBudgetSummaryRepository extends Repository<BudgetItem, Long> {

    @Query(
            value = """
                    select
                        v.budget_item_id as budgetItemId,
                        v.project_id as projectId,
                        v.category_id as categoryId,
                        v.budget_item_description as budgetItemDescription,
                        v.beneficiary_type as beneficiaryType,
                        v.project_people_id as projectPeopleId,
                        v.project_company_id as projectCompanyId,
                        v.beneficiary_name as beneficiaryName,
                        v.beneficiary_role as beneficiaryRole,
                        v.contracted_amount as contractedAmount,
                        v.planned_amount as plannedAmount,
                        v.total_received as totalReceived,
                        v.balance as balance,
                        v.percent_executed as percentExecuted,
                        v.is_over_budget as isOverBudget
                    from vw_beneficiary_budget_summary v
                    where v.project_id = :projectId
                      and (:projectPeopleId is null or v.project_people_id = :projectPeopleId)
                      and (:projectCompanyId is null or v.project_company_id = :projectCompanyId)
                    order by v.budget_item_id
                    """,
            nativeQuery = true
    )
    List<BeneficiaryBudgetSummaryProjection> findSummaryByProject(
            @Param("projectId") Long projectId,
            @Param("projectPeopleId") Long projectPeopleId,
            @Param("projectCompanyId") Long projectCompanyId
    );
}
