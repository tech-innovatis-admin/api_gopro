package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BeneficiaryBudgetSummaryDTO;
import br.com.gopro.api.dtos.BeneficiaryProjectTotalsDTO;

import java.math.BigDecimal;
import java.util.List;

public interface BudgetItemBeneficiaryService {

    /**
     * Vincula beneficiario de rubrica a um item orcamentario.
     */
    void assignBeneficiary(
            Long budgetItemId,
            String beneficiaryType,
            Long referenceId,
            BigDecimal contractedAmount,
            Long actorUserId
    );

    /**
     * Remove beneficiario de rubrica de um item orcamentario.
     */
    void removeBeneficiary(Long budgetItemId, Long actorUserId);

    /**
     * Atualiza valor contratado de um item orcamentario.
     */
    void updateContractedAmount(Long budgetItemId, BigDecimal newAmount, Long actorUserId);

    /**
     * Retorna resumo de rubricas por beneficiario de um projeto.
     */
    List<BeneficiaryBudgetSummaryDTO> getBeneficiarySummaryByProject(Long projectId);

    /**
     * Retorna resumo de rubricas para um vinculo pessoa-projeto.
     */
    List<BeneficiaryBudgetSummaryDTO> getBeneficiarySummaryByPerson(Long projectId, Long projectPeopleId);

    /**
     * Retorna resumo de rubricas para um vinculo empresa-projeto.
     */
    List<BeneficiaryBudgetSummaryDTO> getBeneficiarySummaryByCompany(Long projectId, Long projectCompanyId);

    /**
     * Retorna totais financeiros consolidados de uma pessoa no projeto.
     */
    BeneficiaryProjectTotalsDTO getPersonTotalsInProject(Long projectId, Long projectPeopleId);

    /**
     * Retorna totais financeiros consolidados de uma empresa no projeto.
     */
    BeneficiaryProjectTotalsDTO getCompanyTotalsInProject(Long projectId, Long projectCompanyId);
}

