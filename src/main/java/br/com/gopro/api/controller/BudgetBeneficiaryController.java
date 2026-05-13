package br.com.gopro.api.controller;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.config.SecurityPrincipalUtils;
import br.com.gopro.api.dtos.BeneficiaryBudgetSummaryDTO;
import br.com.gopro.api.dtos.BeneficiaryProjectTotalsDTO;
import br.com.gopro.api.dtos.BudgetItemBeneficiaryAssignRequestDTO;
import br.com.gopro.api.dtos.BudgetItemContractedAmountUpdateDTO;
import br.com.gopro.api.service.BudgetItemBeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "BudgetBeneficiary", description = "Gerenciamento de beneficiarios de rubrica")
@RequestMapping
public class BudgetBeneficiaryController {

    private final BudgetItemBeneficiaryService budgetItemBeneficiaryService;

    @Operation(summary = "Vincular beneficiario ao item de rubrica")
    @PutMapping("/budget-items/{id}/beneficiary")
    public ResponseEntity<Void> assignBeneficiary(
            @PathVariable Long id,
            @Valid @RequestBody BudgetItemBeneficiaryAssignRequestDTO dto,
            Authentication authentication
    ) {
        AuthenticatedUserPrincipal principal = SecurityPrincipalUtils.require(authentication);
        budgetItemBeneficiaryService.assignBeneficiary(
                id,
                dto.beneficiaryType(),
                dto.referenceId(),
                dto.contractedAmount(),
                principal.id()
        );
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar valor contratado do beneficiario no item de rubrica")
    @PatchMapping("/budget-items/{id}/beneficiary/contracted-amount")
    public ResponseEntity<Void> updateContractedAmount(
            @PathVariable Long id,
            @Valid @RequestBody BudgetItemContractedAmountUpdateDTO dto,
            Authentication authentication
    ) {
        AuthenticatedUserPrincipal principal = SecurityPrincipalUtils.require(authentication);
        budgetItemBeneficiaryService.updateContractedAmount(id, dto.contractedAmount(), principal.id());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remover beneficiario do item de rubrica")
    @DeleteMapping("/budget-items/{id}/beneficiary")
    public ResponseEntity<Void> removeBeneficiary(@PathVariable Long id, Authentication authentication) {
        AuthenticatedUserPrincipal principal = SecurityPrincipalUtils.require(authentication);
        budgetItemBeneficiaryService.removeBeneficiary(id, principal.id());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar resumo financeiro de rubricas por beneficiario no projeto")
    @GetMapping("/projects/{projectId}/budget/beneficiary-summary")
    public ResponseEntity<List<BeneficiaryBudgetSummaryDTO>> getSummaryByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long projectPeopleId,
            @RequestParam(required = false) Long projectCompanyId
    ) {
        if (projectPeopleId != null && projectCompanyId != null) {
            throw new IllegalArgumentException("Informe somente projectPeopleId ou projectCompanyId");
        }
        if (projectPeopleId != null) {
            return ResponseEntity.ok(budgetItemBeneficiaryService.getBeneficiarySummaryByPerson(projectId, projectPeopleId));
        }
        if (projectCompanyId != null) {
            return ResponseEntity.ok(budgetItemBeneficiaryService.getBeneficiarySummaryByCompany(projectId, projectCompanyId));
        }
        return ResponseEntity.ok(budgetItemBeneficiaryService.getBeneficiarySummaryByProject(projectId));
    }

    @Operation(summary = "Consolidado financeiro de uma pessoa no projeto")
    @GetMapping("/projects/{projectId}/budget/person-totals/{projectPeopleId}")
    public ResponseEntity<BeneficiaryProjectTotalsDTO> getPersonTotals(
            @PathVariable Long projectId,
            @PathVariable Long projectPeopleId
    ) {
        return ResponseEntity.ok(budgetItemBeneficiaryService.getPersonTotalsInProject(projectId, projectPeopleId));
    }

    @Operation(summary = "Consolidado financeiro de uma empresa no projeto")
    @GetMapping("/projects/{projectId}/budget/company-totals/{projectCompanyId}")
    public ResponseEntity<BeneficiaryProjectTotalsDTO> getCompanyTotals(
            @PathVariable Long projectId,
            @PathVariable Long projectCompanyId
    ) {
        return ResponseEntity.ok(budgetItemBeneficiaryService.getCompanyTotalsInProject(projectId, projectCompanyId));
    }
}

