package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BeneficiaryProjectTotalsDTO;
import br.com.gopro.api.exception.BeneficiaryAlreadyAssignedException;
import br.com.gopro.api.model.BudgetCategory;
import br.com.gopro.api.model.BudgetItem;
import br.com.gopro.api.model.Company;
import br.com.gopro.api.model.People;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.model.ProjectCompany;
import br.com.gopro.api.model.ProjectPeople;
import br.com.gopro.api.repository.BeneficiaryBudgetSummaryRepository;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.ProjectCompanyRepository;
import br.com.gopro.api.repository.ProjectPeopleRepository;
import br.com.gopro.api.repository.projection.BeneficiaryBudgetSummaryProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetItemBeneficiaryServiceImplTest {

    @Mock
    private BudgetItemRepository budgetItemRepository;
    @Mock
    private ProjectPeopleRepository projectPeopleRepository;
    @Mock
    private ProjectCompanyRepository projectCompanyRepository;
    @Mock
    private BeneficiaryBudgetSummaryRepository beneficiaryBudgetSummaryRepository;
    @Mock
    private AuditLogService auditLogService;

    private BudgetItemBeneficiaryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BudgetItemBeneficiaryServiceImpl(
                budgetItemRepository,
                projectPeopleRepository,
                projectCompanyRepository,
                beneficiaryBudgetSummaryRepository,
                auditLogService
        );
    }

    @Test
    void assignBeneficiaryPerson_shouldSucceed() {
        BudgetItem item = activeBudgetItem(100L, 10L);
        ProjectPeople projectPeople = projectPeople(500L, 10L, 700L, "Pessoa Teste");

        when(budgetItemRepository.findById(100L)).thenReturn(Optional.of(item));
        when(projectPeopleRepository.findById(500L)).thenReturn(Optional.of(projectPeople));

        service.assignBeneficiary(100L, "person", 500L, new BigDecimal("1200.00"), 99L);

        assertThat(item.getProjectPeople()).isNotNull();
        assertThat(item.getProjectPeople().getId()).isEqualTo(500L);
        assertThat(item.getProjectCompany()).isNull();
        assertThat(item.getBeneficiaryType()).isEqualTo("person");
        assertThat(item.getContractedAmount()).isEqualByComparingTo("1200.00");
        verify(budgetItemRepository).save(item);
    }

    @Test
    void assignBeneficiaryCompany_shouldSucceed() {
        BudgetItem item = activeBudgetItem(101L, 10L);
        ProjectCompany projectCompany = projectCompany(600L, 10L, 701L, "Empresa Teste");

        when(budgetItemRepository.findById(101L)).thenReturn(Optional.of(item));
        when(projectCompanyRepository.findById(600L)).thenReturn(Optional.of(projectCompany));

        service.assignBeneficiary(101L, "company", 600L, new BigDecimal("3000.00"), 99L);

        assertThat(item.getProjectCompany()).isNotNull();
        assertThat(item.getProjectCompany().getId()).isEqualTo(600L);
        assertThat(item.getProjectPeople()).isNull();
        assertThat(item.getBeneficiaryType()).isEqualTo("company");
        assertThat(item.getContractedAmount()).isEqualByComparingTo("3000.00");
        verify(budgetItemRepository).save(item);
    }

    @Test
    void assignBeneficiaryDifferentProject_shouldThrow() {
        BudgetItem item = activeBudgetItem(102L, 10L);
        ProjectPeople projectPeople = projectPeople(501L, 11L, 702L, "Pessoa Outro Projeto");

        when(budgetItemRepository.findById(102L)).thenReturn(Optional.of(item));
        when(projectPeopleRepository.findById(501L)).thenReturn(Optional.of(projectPeople));

        assertThatThrownBy(() -> service.assignBeneficiary(102L, "person", 501L, new BigDecimal("1500.00"), 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao pertence ao mesmo projeto");
    }

    @Test
    void assignBeneficiaryWhenDifferentAlreadyAssigned_shouldThrow() {
        BudgetItem item = activeBudgetItem(103L, 10L);
        item.setBeneficiaryType("person");
        item.setProjectPeople(projectPeople(502L, 10L, 703L, "Pessoa 1"));

        ProjectCompany newCompany = projectCompany(601L, 10L, 704L, "Empresa Nova");

        when(budgetItemRepository.findById(103L)).thenReturn(Optional.of(item));
        when(projectCompanyRepository.findById(601L)).thenReturn(Optional.of(newCompany));

        assertThatThrownBy(() -> service.assignBeneficiary(103L, "company", 601L, new BigDecimal("2000.00"), 99L))
                .isInstanceOf(BeneficiaryAlreadyAssignedException.class);
    }

    @Test
    void updateContractedAmountNegative_shouldThrow() {
        BudgetItem item = activeBudgetItem(104L, 10L);
        when(budgetItemRepository.findById(104L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.updateContractedAmount(104L, new BigDecimal("-1.00"), 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao pode ser negativo");
    }

    @Test
    void getPersonTotalsInProject_shouldCalculateBalanceAndPercent() {
        ProjectPeople projectPeople = projectPeople(503L, 10L, 705L, "Pessoa Totais");
        when(projectPeopleRepository.findById(503L)).thenReturn(Optional.of(projectPeople));
        when(beneficiaryBudgetSummaryRepository.findSummaryByProject(10L, 503L, null)).thenReturn(List.of(
                projection(1L, 10L, 1L, "Item 1", "person", 503L, null, "Pessoa Totais", "COORD", new BigDecimal("100.00"), new BigDecimal("100.00"), new BigDecimal("80.00")),
                projection(2L, 10L, 1L, "Item 2", "person", 503L, null, "Pessoa Totais", "COORD", new BigDecimal("50.00"), new BigDecimal("50.00"), new BigDecimal("20.00"))
        ));

        BeneficiaryProjectTotalsDTO totals = service.getPersonTotalsInProject(10L, 503L);

        assertThat(totals.contractedAmountTotal()).isEqualByComparingTo("150.00");
        assertThat(totals.totalReceived()).isEqualByComparingTo("100.00");
        assertThat(totals.balance()).isEqualByComparingTo("50.00");
        assertThat(totals.percentExecuted()).isEqualByComparingTo("66.67");
        assertThat(totals.isOverBudget()).isFalse();
        assertThat(totals.items()).hasSize(2);
    }

    @Test
    void getCompanyTotalsInProject_shouldCalculateBalanceAndPercent() {
        ProjectCompany projectCompany = projectCompany(602L, 10L, 706L, "Empresa Totais");
        when(projectCompanyRepository.findById(602L)).thenReturn(Optional.of(projectCompany));
        when(beneficiaryBudgetSummaryRepository.findSummaryByProject(10L, null, 602L)).thenReturn(List.of(
                projection(3L, 10L, 2L, "Item A", "company", null, 602L, "Empresa Totais", "SERVICO", new BigDecimal("200.00"), new BigDecimal("200.00"), new BigDecimal("250.00"))
        ));

        BeneficiaryProjectTotalsDTO totals = service.getCompanyTotalsInProject(10L, 602L);

        assertThat(totals.contractedAmountTotal()).isEqualByComparingTo("200.00");
        assertThat(totals.totalReceived()).isEqualByComparingTo("250.00");
        assertThat(totals.balance()).isEqualByComparingTo("-50.00");
        assertThat(totals.percentExecuted()).isEqualByComparingTo("125.00");
        assertThat(totals.isOverBudget()).isTrue();
        assertThat(totals.items()).hasSize(1);
    }

    @Test
    void removeBeneficiary_shouldClearFieldsAndAudit() {
        BudgetItem item = activeBudgetItem(105L, 10L);
        item.setBeneficiaryType("person");
        item.setProjectPeople(projectPeople(504L, 10L, 707L, "Pessoa Remove"));
        item.setContractedAmount(new BigDecimal("999.00"));
        when(budgetItemRepository.findById(105L)).thenReturn(Optional.of(item));

        service.removeBeneficiary(105L, 99L);

        ArgumentCaptor<BudgetItem> captor = ArgumentCaptor.forClass(BudgetItem.class);
        verify(budgetItemRepository).save(captor.capture());
        BudgetItem saved = captor.getValue();
        assertThat(saved.getProjectPeople()).isNull();
        assertThat(saved.getProjectCompany()).isNull();
        assertThat(saved.getBeneficiaryType()).isNull();
        assertThat(saved.getContractedAmount()).isNull();
        verify(auditLogService).log(any(), eq(null));
    }

    private static BudgetItem activeBudgetItem(Long id, Long projectId) {
        Project project = new Project();
        project.setId(projectId);

        BudgetCategory category = new BudgetCategory();
        category.setId(1L);
        category.setProject(project);

        BudgetItem item = new BudgetItem();
        item.setId(id);
        item.setCategory(category);
        item.setIsActive(true);
        return item;
    }

    private static ProjectPeople projectPeople(Long projectPeopleId, Long projectId, Long personId, String personName) {
        Project project = new Project();
        project.setId(projectId);

        People people = new People();
        people.setId(personId);
        people.setFullName(personName);

        ProjectPeople projectPeople = new ProjectPeople();
        projectPeople.setId(projectPeopleId);
        projectPeople.setProject(project);
        projectPeople.setPerson(people);
        return projectPeople;
    }

    private static ProjectCompany projectCompany(Long projectCompanyId, Long projectId, Long companyId, String companyName) {
        Project project = new Project();
        project.setId(projectId);

        Company company = new Company();
        company.setId(companyId);
        company.setName(companyName);

        ProjectCompany projectCompany = new ProjectCompany();
        projectCompany.setId(projectCompanyId);
        projectCompany.setProject(project);
        projectCompany.setCompany(company);
        projectCompany.setServiceType("SERVICO");
        return projectCompany;
    }

    private static BeneficiaryBudgetSummaryProjection projection(
            Long budgetItemId,
            Long projectId,
            Long categoryId,
            String budgetItemDescription,
            String beneficiaryType,
            Long projectPeopleId,
            Long projectCompanyId,
            String beneficiaryName,
            String beneficiaryRole,
            BigDecimal contractedAmount,
            BigDecimal plannedAmount,
            BigDecimal totalReceived
    ) {
        BigDecimal balance = contractedAmount.subtract(totalReceived);
        BigDecimal percent = contractedAmount.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalReceived.multiply(new BigDecimal("100")).divide(contractedAmount, 2, java.math.RoundingMode.HALF_UP);
        boolean overBudget = totalReceived.compareTo(contractedAmount) > 0;

        return new BeneficiaryBudgetSummaryProjection() {
            @Override
            public Long getBudgetItemId() { return budgetItemId; }
            @Override
            public Long getProjectId() { return projectId; }
            @Override
            public Long getCategoryId() { return categoryId; }
            @Override
            public String getBudgetItemDescription() { return budgetItemDescription; }
            @Override
            public String getBeneficiaryType() { return beneficiaryType; }
            @Override
            public Long getProjectPeopleId() { return projectPeopleId; }
            @Override
            public Long getProjectCompanyId() { return projectCompanyId; }
            @Override
            public String getBeneficiaryName() { return beneficiaryName; }
            @Override
            public String getBeneficiaryRole() { return beneficiaryRole; }
            @Override
            public BigDecimal getContractedAmount() { return contractedAmount; }
            @Override
            public BigDecimal getPlannedAmount() { return plannedAmount; }
            @Override
            public BigDecimal getTotalReceived() { return totalReceived; }
            @Override
            public BigDecimal getBalance() { return balance; }
            @Override
            public BigDecimal getPercentExecuted() { return percent; }
            @Override
            public Boolean getIsOverBudget() { return overBudget; }
        };
    }
}
