package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BeneficiaryBudgetSummaryDTO;
import br.com.gopro.api.dtos.BeneficiaryProjectTotalsDTO;
import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.exception.BeneficiaryAlreadyAssignedException;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.model.BudgetItem;
import br.com.gopro.api.model.ProjectCompany;
import br.com.gopro.api.model.ProjectPeople;
import br.com.gopro.api.repository.BeneficiaryBudgetSummaryRepository;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.ProjectCompanyRepository;
import br.com.gopro.api.repository.ProjectPeopleRepository;
import br.com.gopro.api.repository.projection.BeneficiaryBudgetSummaryProjection;
import br.com.gopro.api.service.audit.AuditEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BudgetItemBeneficiaryServiceImpl implements BudgetItemBeneficiaryService {

    private final BudgetItemRepository budgetItemRepository;
    private final ProjectPeopleRepository projectPeopleRepository;
    private final ProjectCompanyRepository projectCompanyRepository;
    private final BeneficiaryBudgetSummaryRepository beneficiaryBudgetSummaryRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public void assignBeneficiary(
            Long budgetItemId,
            String beneficiaryType,
            Long referenceId,
            BigDecimal contractedAmount,
            Long actorUserId
    ) {
        BudgetItem budgetItem = loadActiveBudgetItem(budgetItemId);
        Long projectId = resolveProjectId(budgetItem);
        String normalizedType = normalizeBeneficiaryType(beneficiaryType);
        validateContractedAmount(contractedAmount, true);

        Map<String, Object> before = snapshotBeneficiaryFields(budgetItem);

        try {
            if ("person".equals(normalizedType)) {
                ProjectPeople projectPeople = projectPeopleRepository.findById(referenceId)
                        .orElseThrow(() -> new ResourceNotFoundException("Vinculo pessoa-projeto nao encontrado"));
                if (!projectId.equals(projectPeople.getProject().getId())) {
                    throw new IllegalArgumentException("Vinculo pessoa-projeto nao pertence ao mesmo projeto do item orcamentario");
                }
                rejectIfDifferentBeneficiaryAlreadyAssigned(budgetItem, "person", referenceId);
                budgetItem.setProjectPeople(projectPeople);
                budgetItem.setProjectCompany(null);
            } else {
                ProjectCompany projectCompany = projectCompanyRepository.findById(referenceId)
                        .orElseThrow(() -> new ResourceNotFoundException("Vinculo empresa-projeto nao encontrado"));
                if (!projectId.equals(projectCompany.getProject().getId())) {
                    throw new IllegalArgumentException("Vinculo empresa-projeto nao pertence ao mesmo projeto do item orcamentario");
                }
                rejectIfDifferentBeneficiaryAlreadyAssigned(budgetItem, "company", referenceId);
                budgetItem.setProjectCompany(projectCompany);
                budgetItem.setProjectPeople(null);
            }

            budgetItem.setBeneficiaryType(normalizedType);
            budgetItem.setContractedAmount(contractedAmount);
            budgetItem.setUpdatedBy(actorUserId);
            budgetItemRepository.save(budgetItem);

            logAudit(actorUserId, "ASSIGN_BENEFICIARY", budgetItemId, before, snapshotBeneficiaryFields(budgetItem), AuditResultEnum.SUCESSO);
        } catch (RuntimeException exception) {
            logAudit(actorUserId, "ASSIGN_BENEFICIARY", budgetItemId, before, snapshotBeneficiaryFields(budgetItem), AuditResultEnum.FALHA);
            throw exception;
        }
    }

    @Override
    @Transactional
    public void removeBeneficiary(Long budgetItemId, Long actorUserId) {
        BudgetItem budgetItem = loadActiveBudgetItem(budgetItemId);
        Map<String, Object> before = snapshotBeneficiaryFields(budgetItem);
        try {
            budgetItem.setProjectPeople(null);
            budgetItem.setProjectCompany(null);
            budgetItem.setBeneficiaryType(null);
            budgetItem.setContractedAmount(null);
            budgetItem.setUpdatedBy(actorUserId);
            budgetItemRepository.save(budgetItem);

            logAudit(actorUserId, "REMOVE_BENEFICIARY", budgetItemId, before, snapshotBeneficiaryFields(budgetItem), AuditResultEnum.SUCESSO);
        } catch (RuntimeException exception) {
            logAudit(actorUserId, "REMOVE_BENEFICIARY", budgetItemId, before, snapshotBeneficiaryFields(budgetItem), AuditResultEnum.FALHA);
            throw exception;
        }
    }

    @Override
    @Transactional
    public void updateContractedAmount(Long budgetItemId, BigDecimal newAmount, Long actorUserId) {
        BudgetItem budgetItem = loadActiveBudgetItem(budgetItemId);
        validateContractedAmount(newAmount, false);
        Map<String, Object> before = snapshotBeneficiaryFields(budgetItem);
        try {
            budgetItem.setContractedAmount(newAmount);
            budgetItem.setUpdatedBy(actorUserId);
            budgetItemRepository.save(budgetItem);
            logAudit(actorUserId, "UPDATE_CONTRACTED_AMOUNT", budgetItemId, before, snapshotBeneficiaryFields(budgetItem), AuditResultEnum.SUCESSO);
        } catch (RuntimeException exception) {
            logAudit(actorUserId, "UPDATE_CONTRACTED_AMOUNT", budgetItemId, before, snapshotBeneficiaryFields(budgetItem), AuditResultEnum.FALHA);
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryBudgetSummaryDTO> getBeneficiarySummaryByProject(Long projectId) {
        return mapSummaryRows(beneficiaryBudgetSummaryRepository.findSummaryByProject(projectId, null, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryBudgetSummaryDTO> getBeneficiarySummaryByPerson(Long projectId, Long projectPeopleId) {
        return mapSummaryRows(beneficiaryBudgetSummaryRepository.findSummaryByProject(projectId, projectPeopleId, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryBudgetSummaryDTO> getBeneficiarySummaryByCompany(Long projectId, Long projectCompanyId) {
        return mapSummaryRows(beneficiaryBudgetSummaryRepository.findSummaryByProject(projectId, null, projectCompanyId));
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaryProjectTotalsDTO getPersonTotalsInProject(Long projectId, Long projectPeopleId) {
        ProjectPeople projectPeople = projectPeopleRepository.findById(projectPeopleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo pessoa-projeto nao encontrado"));
        if (!projectId.equals(projectPeople.getProject().getId())) {
            throw new IllegalArgumentException("Vinculo pessoa-projeto nao pertence ao projeto informado");
        }

        List<BeneficiaryBudgetSummaryDTO> items = getBeneficiarySummaryByPerson(projectId, projectPeopleId);
        return buildTotals(
                "person",
                projectPeopleId,
                projectPeople.getPerson().getFullName(),
                projectPeople.getRole() != null ? projectPeople.getRole().name() : null,
                items
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaryProjectTotalsDTO getCompanyTotalsInProject(Long projectId, Long projectCompanyId) {
        ProjectCompany projectCompany = projectCompanyRepository.findById(projectCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo empresa-projeto nao encontrado"));
        if (!projectId.equals(projectCompany.getProject().getId())) {
            throw new IllegalArgumentException("Vinculo empresa-projeto nao pertence ao projeto informado");
        }

        List<BeneficiaryBudgetSummaryDTO> items = getBeneficiarySummaryByCompany(projectId, projectCompanyId);
        return buildTotals(
                "company",
                projectCompanyId,
                projectCompany.getCompany().getName(),
                projectCompany.getServiceType(),
                items
        );
    }

    private BudgetItem loadActiveBudgetItem(Long budgetItemId) {
        BudgetItem budgetItem = budgetItemRepository.findById(budgetItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item orcamentario nao encontrado"));
        if (!Boolean.TRUE.equals(budgetItem.getIsActive())) {
            throw new BusinessException("Item orcamentario inativo");
        }
        return budgetItem;
    }

    private Long resolveProjectId(BudgetItem budgetItem) {
        if (budgetItem.getCategory() == null || budgetItem.getCategory().getProject() == null || budgetItem.getCategory().getProject().getId() == null) {
            throw new BusinessException("Item orcamentario sem projeto associado");
        }
        return budgetItem.getCategory().getProject().getId();
    }

    private String normalizeBeneficiaryType(String beneficiaryType) {
        if (beneficiaryType == null || beneficiaryType.isBlank()) {
            throw new IllegalArgumentException("beneficiaryType e obrigatorio");
        }
        String normalized = beneficiaryType.trim().toLowerCase(Locale.ROOT);
        if (!"person".equals(normalized) && !"company".equals(normalized)) {
            throw new IllegalArgumentException("beneficiaryType deve ser 'person' ou 'company'");
        }
        return normalized;
    }

    private void validateContractedAmount(BigDecimal value, boolean rejectZero) {
        if (value == null) {
            throw new IllegalArgumentException("contractedAmount e obrigatorio");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("contractedAmount nao pode ser negativo");
        }
        if (rejectZero && value.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("contractedAmount deve ser maior que zero");
        }
    }

    private void rejectIfDifferentBeneficiaryAlreadyAssigned(BudgetItem budgetItem, String incomingType, Long incomingReferenceId) {
        Long existingPersonId = budgetItem.getProjectPeople() != null ? budgetItem.getProjectPeople().getId() : null;
        Long existingCompanyId = budgetItem.getProjectCompany() != null ? budgetItem.getProjectCompany().getId() : null;
        String existingType = budgetItem.getBeneficiaryType();

        if (existingPersonId == null && existingCompanyId == null && existingType == null) {
            return;
        }

        boolean same = ("person".equals(incomingType) && incomingReferenceId.equals(existingPersonId))
                || ("company".equals(incomingType) && incomingReferenceId.equals(existingCompanyId));
        if (!same) {
            throw new BeneficiaryAlreadyAssignedException("Este item orcamentario ja possui beneficiario vinculado");
        }
    }

    private Map<String, Object> snapshotBeneficiaryFields(BudgetItem budgetItem) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("projectPeopleId", budgetItem.getProjectPeople() != null ? budgetItem.getProjectPeople().getId() : null);
        snapshot.put("projectCompanyId", budgetItem.getProjectCompany() != null ? budgetItem.getProjectCompany().getId() : null);
        snapshot.put("beneficiaryType", budgetItem.getBeneficiaryType());
        snapshot.put("contractedAmount", budgetItem.getContractedAmount());
        return snapshot;
    }

    private void logAudit(
            Long actorUserId,
            String action,
            Long budgetItemId,
            Map<String, Object> before,
            Map<String, Object> after,
            AuditResultEnum result
    ) {
        auditLogService.log(
                AuditEventRequest.builder()
                        .actorUserId(actorUserId)
                        .tipoAuditoria(AuditScopeEnum.CONTRACTS)
                        .modulo("FINANCEIRO")
                        .feature("RUBRICA_BENEFICIARIO")
                        .entidadePrincipal("BUDGET_ITEM")
                        .entidadeId(String.valueOf(budgetItemId))
                        .acao(action)
                        .antes(before)
                        .depois(after)
                        .resultado(result)
                        .build(),
                null
        );
    }

    private List<BeneficiaryBudgetSummaryDTO> mapSummaryRows(List<BeneficiaryBudgetSummaryProjection> rows) {
        return rows.stream()
                .map(row -> new BeneficiaryBudgetSummaryDTO(
                        row.getBudgetItemId(),
                        row.getProjectId(),
                        row.getCategoryId(),
                        row.getBudgetItemDescription(),
                        row.getBeneficiaryType(),
                        row.getProjectPeopleId(),
                        row.getProjectCompanyId(),
                        row.getBeneficiaryName(),
                        row.getBeneficiaryRole(),
                        row.getContractedAmount(),
                        row.getPlannedAmount(),
                        row.getTotalReceived(),
                        row.getBalance(),
                        row.getPercentExecuted(),
                        row.getIsOverBudget()
                ))
                .toList();
    }

    private BeneficiaryProjectTotalsDTO buildTotals(
            String beneficiaryType,
            Long beneficiaryId,
            String beneficiaryName,
            String beneficiaryRole,
            List<BeneficiaryBudgetSummaryDTO> items
    ) {
        BigDecimal contractedAmountTotal = items.stream()
                .map(BeneficiaryBudgetSummaryDTO::contractedAmount)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalReceived = items.stream()
                .map(BeneficiaryBudgetSummaryDTO::totalReceived)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = contractedAmountTotal.subtract(totalReceived);
        BigDecimal percentExecuted = contractedAmountTotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalReceived
                .divide(contractedAmountTotal, 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

        return new BeneficiaryProjectTotalsDTO(
                beneficiaryType,
                beneficiaryId,
                beneficiaryName,
                beneficiaryRole,
                contractedAmountTotal,
                totalReceived,
                balance,
                percentExecuted,
                totalReceived.compareTo(contractedAmountTotal) > 0,
                items
        );
    }
}
