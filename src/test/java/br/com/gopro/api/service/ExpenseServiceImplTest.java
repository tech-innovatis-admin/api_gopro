package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ExpenseRequestDTO;
import br.com.gopro.api.dtos.ExpenseResponseDTO;
import br.com.gopro.api.dtos.ExpenseUpdateDTO;
import br.com.gopro.api.enums.ContractingStatusEnum;
import br.com.gopro.api.enums.ExpensePaidByEnum;
import br.com.gopro.api.enums.ExpensePaymentStatusEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.FieldValidationException;
import br.com.gopro.api.mapper.ExpenseMapper;
import br.com.gopro.api.model.BudgetCategory;
import br.com.gopro.api.model.BudgetItem;
import br.com.gopro.api.model.Expense;
import br.com.gopro.api.model.Income;
import br.com.gopro.api.model.Organization;
import br.com.gopro.api.model.People;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.model.ProjectCompany;
import br.com.gopro.api.repository.BudgetCategoryRepository;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.DocumentRepository;
import br.com.gopro.api.repository.ExpenseRepository;
import br.com.gopro.api.repository.IncomeRepository;
import br.com.gopro.api.repository.OrganizationRepository;
import br.com.gopro.api.repository.PeopleRepository;
import br.com.gopro.api.repository.ProjectCompanyRepository;
import br.com.gopro.api.repository.ProjectPeopleRepository;
import br.com.gopro.api.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private BudgetItemRepository budgetItemRepository;

    @Mock
    private BudgetCategoryRepository budgetCategoryRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private PeopleRepository peopleRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ProjectCompanyRepository projectCompanyRepository;

    @Mock
    private ProjectPeopleRepository projectPeopleRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectFinancialSummaryService projectFinancialSummaryService;

    @Mock
    private ProjectCompanyFinancialValidationService projectCompanyFinancialValidationService;

    @InjectMocks
    private ExpenseServiceImpl service;

    @Test
    void createExpense_shouldLinkExpenseToProjectAndClearLegacyIncomeReference() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO(
                55L,
                11L,
                12L,
                99L,
                LocalDate.of(2026, 3, 17),
                1,
                new BigDecimal("250.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.INNOVATIS,
                null,
                null,
                null,
                "Servico especializado",
                null,
                null,
                null,
                7L
        );

        Expense mappedExpense = new Expense();
        Project project = project(55L);
        BudgetItem budgetItem = budgetItem(11L);
        BudgetCategory category = category(12L);

        when(expenseMapper.toEntity(dto)).thenReturn(mappedExpense);
        when(projectRepository.getReferenceById(55L)).thenReturn(project);
        when(budgetItemRepository.getReferenceById(11L)).thenReturn(budgetItem);
        when(budgetCategoryRepository.getReferenceById(12L)).thenReturn(category);
        when(budgetItemRepository.findProjectIdById(11L)).thenReturn(Optional.of(55L));
        when(budgetCategoryRepository.findProjectIdById(12L)).thenReturn(Optional.of(55L));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            saved.setId(321L);
            return saved;
        });
        when(expenseMapper.toDTO(any(Expense.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        ExpenseResponseDTO result = service.createExpense(dto);

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(expenseCaptor.capture());
        Expense savedExpense = expenseCaptor.getValue();

        assertThat(savedExpense.getProject()).isSameAs(project);
        assertThat(savedExpense.getBudgetItem()).isSameAs(budgetItem);
        assertThat(savedExpense.getCategory()).isSameAs(category);
        assertThat(savedExpense.getIncome()).isNull();
        assertThat(savedExpense.getIsActive()).isTrue();
        assertThat(result.projectId()).isEqualTo(55L);
        assertThat(result.incomeId()).isNull();
    }

    @Test
    void createExpense_shouldDeriveProjectFromIncomeWhenProjectIdIsMissing() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO(
                null,
                11L,
                12L,
                99L,
                LocalDate.of(2026, 3, 17),
                1,
                new BigDecimal("80.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.INNOVATIS,
                null,
                null,
                null,
                "Despesa derivada",
                null,
                null,
                null,
                7L
        );

        Expense mappedExpense = new Expense();
        Project project = project(55L);

        when(expenseMapper.toEntity(dto)).thenReturn(mappedExpense);
        when(incomeRepository.findProjectIdById(99L)).thenReturn(Optional.of(55L));
        when(projectRepository.getReferenceById(55L)).thenReturn(project);
        when(budgetItemRepository.getReferenceById(11L)).thenReturn(budgetItem(11L));
        when(budgetCategoryRepository.getReferenceById(12L)).thenReturn(category(12L));
        when(budgetItemRepository.findProjectIdById(11L)).thenReturn(Optional.of(55L));
        when(budgetCategoryRepository.findProjectIdById(12L)).thenReturn(Optional.of(55L));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(expenseMapper.toDTO(any(Expense.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        ExpenseResponseDTO result = service.createExpense(dto);

        assertThat(result.projectId()).isEqualTo(55L);
        assertThat(result.incomeId()).isNull();
    }

    @Test
    void createExpense_shouldRejectRequestsWithoutProjectOrIncomeReference() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO(
                null,
                11L,
                12L,
                null,
                LocalDate.of(2026, 3, 17),
                1,
                new BigDecimal("80.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.INNOVATIS,
                null,
                null,
                null,
                "Despesa invalida",
                null,
                null,
                null,
                7L
        );

        when(expenseMapper.toEntity(dto)).thenReturn(new Expense());

        assertThatThrownBy(() -> service.createExpense(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Projeto da despesa e obrigatorio");
    }

    @Test
    void updateExpense_shouldMoveExpenseToProjectAndRemoveIncomeBinding() {
        Expense existingExpense = new Expense();
        existingExpense.setId(777L);
        existingExpense.setIsActive(true);
        existingExpense.setProject(project(10L));
        existingExpense.setIncome(new Income());
        existingExpense.getIncome().setId(999L);
        existingExpense.setBudgetItem(budgetItem(11L));
        existingExpense.setCategory(category(12L));
        existingExpense.setExpenseDate(LocalDate.of(2026, 3, 10));
        existingExpense.setQuantity(1);
        existingExpense.setAmount(new BigDecimal("100.00"));
        existingExpense.setDescription("Despesa antiga");

        ExpenseUpdateDTO dto = new ExpenseUpdateDTO(
                77L,
                null,
                null,
                null,
                LocalDate.of(2026, 3, 17),
                2,
                new BigDecimal("150.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.EXECUCAO,
                null,
                null,
                null,
                "Despesa atualizada",
                null,
                null,
                null,
                8L
        );

        when(expenseRepository.findById(777L)).thenReturn(Optional.of(existingExpense));
        when(projectRepository.getReferenceById(77L)).thenReturn(project(77L));
        when(budgetItemRepository.findProjectIdById(11L)).thenReturn(Optional.of(77L));
        when(budgetCategoryRepository.findProjectIdById(12L)).thenReturn(Optional.of(77L));
        doAnswer(invocation -> {
            ExpenseUpdateDTO update = invocation.getArgument(0);
            Expense expense = invocation.getArgument(1);
            if (update.expenseDate() != null) {
                expense.setExpenseDate(update.expenseDate());
            }
            if (update.quantity() != null) {
                expense.setQuantity(update.quantity());
            }
            if (update.amount() != null) {
                expense.setAmount(update.amount());
            }
            if (update.paidBy() != null) {
                expense.setPaidBy(update.paidBy());
            }
            if (update.description() != null) {
                expense.setDescription(update.description());
            }
            if (update.updatedBy() != null) {
                expense.setUpdatedBy(update.updatedBy());
            }
            return null;
        }).when(expenseMapper).updateEntityFromDTO(any(ExpenseUpdateDTO.class), any(Expense.class));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(expenseMapper.toDTO(any(Expense.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        ExpenseResponseDTO result = service.updateExpenseById(777L, dto);

        assertThat(existingExpense.getProject().getId()).isEqualTo(77L);
        assertThat(existingExpense.getIncome()).isNull();
        assertThat(existingExpense.getExpenseDate()).isEqualTo(LocalDate.of(2026, 3, 17));
        assertThat(existingExpense.getQuantity()).isEqualTo(2);
        assertThat(existingExpense.getAmount()).isEqualByComparingTo("150.00");
        assertThat(existingExpense.getPaidBy()).isEqualTo(ExpensePaidByEnum.EXECUCAO);
        assertThat(existingExpense.getDescription()).isEqualTo("Despesa atualizada");
        assertThat(result.projectId()).isEqualTo(77L);
        assertThat(result.incomeId()).isNull();
    }

    @Test
    void updateExpense_shouldClearPersonAndOrganizationWhenPaymentBecomesUnlinked() {
        Expense existingExpense = new Expense();
        existingExpense.setId(778L);
        existingExpense.setIsActive(true);
        existingExpense.setProject(project(10L));
        existingExpense.setBudgetItem(budgetItem(11L));
        existingExpense.setCategory(category(12L));
        existingExpense.setExpenseDate(LocalDate.of(2026, 3, 10));
        existingExpense.setQuantity(1);
        existingExpense.setAmount(new BigDecimal("100.00"));
        existingExpense.setDescription("Despesa antiga");

        People person = new People();
        person.setId(44L);
        existingExpense.setPerson(person);

        Organization organization = new Organization();
        organization.setId(80L);
        existingExpense.setOrganization(organization);

        ExpenseUpdateDTO dto = new ExpenseUpdateDTO(
                10L,
                11L,
                12L,
                null,
                LocalDate.of(2026, 3, 10),
                1,
                new BigDecimal("100.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.INNOVATIS,
                null,
                null,
                null,
                "Despesa antiga",
                null,
                null,
                null,
                8L
        );

        when(expenseRepository.findById(778L)).thenReturn(Optional.of(existingExpense));
        when(projectRepository.getReferenceById(10L)).thenReturn(project(10L));
        when(budgetItemRepository.getReferenceById(11L)).thenReturn(budgetItem(11L));
        when(budgetCategoryRepository.getReferenceById(12L)).thenReturn(category(12L));
        when(budgetItemRepository.findProjectIdById(11L)).thenReturn(Optional.of(10L));
        when(budgetCategoryRepository.findProjectIdById(12L)).thenReturn(Optional.of(10L));
        doAnswer(invocation -> null)
                .when(expenseMapper).updateEntityFromDTO(any(ExpenseUpdateDTO.class), any(Expense.class));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(expenseMapper.toDTO(any(Expense.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        ExpenseResponseDTO result = service.updateExpenseById(778L, dto);

        assertThat(existingExpense.getPerson()).isNull();
        assertThat(existingExpense.getProjectCompany()).isNull();
        assertThat(existingExpense.getOrganization()).isNull();
        assertThat(result.personId()).isNull();
        assertThat(result.projectCompanyId()).isNull();
        assertThat(result.organizationId()).isNull();
    }

    @Test
    void createExpense_shouldPersistWhenProjectCompanyBelongsToSameProject() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO(
                55L,
                11L,
                12L,
                null,
                LocalDate.of(2026, 3, 17),
                1,
                new BigDecimal("250.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.INNOVATIS,
                null,
                500L,
                null,
                "Servico empresa projeto",
                null,
                null,
                null,
                7L
        );

        Expense mappedExpense = new Expense();
        mappedExpense.setAmount(new BigDecimal("250.00"));
        Project project = project(55L);
        BudgetItem budgetItem = budgetItem(11L);
        BudgetCategory category = category(12L);
        ProjectCompany projectCompany = projectCompany(500L, 55L, ContractingStatusEnum.CONTRATADA, true);

        when(expenseMapper.toEntity(dto)).thenReturn(mappedExpense);
        when(projectRepository.getReferenceById(55L)).thenReturn(project);
        when(budgetItemRepository.getReferenceById(11L)).thenReturn(budgetItem);
        when(budgetCategoryRepository.getReferenceById(12L)).thenReturn(category);
        when(projectCompanyRepository.getReferenceById(500L)).thenReturn(projectCompany);
        when(projectCompanyRepository.findProjectIdById(500L)).thenReturn(Optional.of(55L));
        when(budgetItemRepository.findProjectIdById(11L)).thenReturn(Optional.of(55L));
        when(budgetCategoryRepository.findProjectIdById(12L)).thenReturn(Optional.of(55L));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(expenseMapper.toDTO(any(Expense.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        ExpenseResponseDTO result = service.createExpense(dto);

        assertThat(result.projectCompanyId()).isEqualTo(500L);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void createExpense_shouldPersistWhenPersonBelongsToSameProject() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO(
                55L,
                11L,
                12L,
                null,
                LocalDate.of(2026, 3, 17),
                1,
                new BigDecimal("120.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.INNOVATIS,
                700L,
                null,
                null,
                "Servico pessoa projeto",
                null,
                null,
                null,
                7L
        );

        Expense mappedExpense = new Expense();
        mappedExpense.setAmount(new BigDecimal("120.00"));
        People person = new People();
        person.setId(700L);

        when(expenseMapper.toEntity(dto)).thenReturn(mappedExpense);
        when(projectRepository.getReferenceById(55L)).thenReturn(project(55L));
        when(budgetItemRepository.getReferenceById(11L)).thenReturn(budgetItem(11L));
        when(budgetCategoryRepository.getReferenceById(12L)).thenReturn(category(12L));
        when(peopleRepository.getReferenceById(700L)).thenReturn(person);
        when(projectPeopleRepository.existsByProject_IdAndPerson_IdAndIsActiveTrue(55L, 700L)).thenReturn(true);
        when(budgetItemRepository.findProjectIdById(11L)).thenReturn(Optional.of(55L));
        when(budgetCategoryRepository.findProjectIdById(12L)).thenReturn(Optional.of(55L));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(expenseMapper.toDTO(any(Expense.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        ExpenseResponseDTO result = service.createExpense(dto);

        assertThat(result.personId()).isEqualTo(700L);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void createExpense_shouldRejectWhenProjectCompanyBelongsToAnotherProject() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO(
                55L,
                11L,
                12L,
                null,
                LocalDate.of(2026, 3, 17),
                1,
                new BigDecimal("250.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.INNOVATIS,
                null,
                500L,
                null,
                "Servico empresa errada",
                null,
                null,
                null,
                7L
        );

        ProjectCompany projectCompany = projectCompany(500L, 99L, ContractingStatusEnum.CONTRATADA, true);
        when(expenseMapper.toEntity(dto)).thenReturn(new Expense());
        when(projectRepository.getReferenceById(55L)).thenReturn(project(55L));
        when(budgetItemRepository.getReferenceById(11L)).thenReturn(budgetItem(11L));
        when(budgetCategoryRepository.getReferenceById(12L)).thenReturn(category(12L));
        when(projectCompanyRepository.getReferenceById(500L)).thenReturn(projectCompany);
        when(projectCompanyRepository.findProjectIdById(500L)).thenReturn(Optional.of(99L));
        when(budgetItemRepository.findProjectIdById(11L)).thenReturn(Optional.of(55L));
        when(budgetCategoryRepository.findProjectIdById(12L)).thenReturn(Optional.of(55L));

        assertThatThrownBy(() -> service.createExpense(dto))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(exception -> assertThat(((FieldValidationException) exception).getFieldErrors())
                        .containsEntry("projectCompanyId", "Empresa contratada nao pertence ao projeto informado."));

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createExpense_shouldRejectWhenPersonDoesNotBelongToProject() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO(
                55L,
                11L,
                12L,
                null,
                LocalDate.of(2026, 3, 17),
                1,
                new BigDecimal("120.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.INNOVATIS,
                700L,
                null,
                null,
                "Servico pessoa errada",
                null,
                null,
                null,
                7L
        );

        People person = new People();
        person.setId(700L);

        when(expenseMapper.toEntity(dto)).thenReturn(new Expense());
        when(projectRepository.getReferenceById(55L)).thenReturn(project(55L));
        when(budgetItemRepository.getReferenceById(11L)).thenReturn(budgetItem(11L));
        when(budgetCategoryRepository.getReferenceById(12L)).thenReturn(category(12L));
        when(peopleRepository.getReferenceById(700L)).thenReturn(person);
        when(projectPeopleRepository.existsByProject_IdAndPerson_IdAndIsActiveTrue(55L, 700L)).thenReturn(false);
        when(budgetItemRepository.findProjectIdById(11L)).thenReturn(Optional.of(55L));
        when(budgetCategoryRepository.findProjectIdById(12L)).thenReturn(Optional.of(55L));

        assertThatThrownBy(() -> service.createExpense(dto))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(exception -> assertThat(((FieldValidationException) exception).getFieldErrors())
                        .containsEntry("personId", "Pessoa vinculada nao pertence ao projeto informado."));

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createExpense_shouldRejectWhenBudgetItemBelongsToAnotherProject() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO(
                55L,
                11L,
                12L,
                null,
                LocalDate.of(2026, 3, 17),
                1,
                new BigDecimal("120.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.INNOVATIS,
                null,
                null,
                null,
                "Item errado",
                null,
                null,
                null,
                7L
        );

        when(expenseMapper.toEntity(dto)).thenReturn(new Expense());
        when(projectRepository.getReferenceById(55L)).thenReturn(project(55L));
        when(budgetItemRepository.getReferenceById(11L)).thenReturn(budgetItem(11L));
        when(budgetCategoryRepository.getReferenceById(12L)).thenReturn(category(12L));
        when(budgetItemRepository.findProjectIdById(11L)).thenReturn(Optional.of(99L));
        when(budgetCategoryRepository.findProjectIdById(12L)).thenReturn(Optional.of(55L));

        assertThatThrownBy(() -> service.createExpense(dto))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(exception -> assertThat(((FieldValidationException) exception).getFieldErrors())
                        .containsEntry("budgetItemId", "Item orcamentario nao pertence ao projeto informado."));

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createExpense_shouldRejectWhenCategoryBelongsToAnotherProject() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO(
                55L,
                11L,
                12L,
                null,
                LocalDate.of(2026, 3, 17),
                1,
                new BigDecimal("120.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.INNOVATIS,
                null,
                null,
                null,
                "Categoria errada",
                null,
                null,
                null,
                7L
        );

        when(expenseMapper.toEntity(dto)).thenReturn(new Expense());
        when(projectRepository.getReferenceById(55L)).thenReturn(project(55L));
        when(budgetItemRepository.getReferenceById(11L)).thenReturn(budgetItem(11L));
        when(budgetCategoryRepository.getReferenceById(12L)).thenReturn(category(12L));
        when(budgetItemRepository.findProjectIdById(11L)).thenReturn(Optional.of(55L));
        when(budgetCategoryRepository.findProjectIdById(12L)).thenReturn(Optional.of(99L));

        assertThatThrownBy(() -> service.createExpense(dto))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(exception -> assertThat(((FieldValidationException) exception).getFieldErrors())
                        .containsEntry("categoryId", "Categoria orcamentaria nao pertence ao projeto informado."));

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createExpense_shouldRejectWhenProjectCompanyIsCancelled() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO(
                55L,
                11L,
                12L,
                null,
                LocalDate.of(2026, 3, 17),
                1,
                new BigDecimal("120.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.INNOVATIS,
                null,
                500L,
                null,
                "Empresa cancelada",
                null,
                null,
                null,
                7L
        );

        ProjectCompany projectCompany = projectCompany(500L, 55L, ContractingStatusEnum.CANCELADA, true);

        Expense mappedExpense = new Expense();
        mappedExpense.setAmount(new BigDecimal("120.00"));
        when(expenseMapper.toEntity(dto)).thenReturn(mappedExpense);
        when(projectRepository.getReferenceById(55L)).thenReturn(project(55L));
        when(budgetItemRepository.getReferenceById(11L)).thenReturn(budgetItem(11L));
        when(budgetCategoryRepository.getReferenceById(12L)).thenReturn(category(12L));
        when(projectCompanyRepository.getReferenceById(500L)).thenReturn(projectCompany);
        when(projectCompanyRepository.findProjectIdById(500L)).thenReturn(Optional.of(55L));
        when(budgetItemRepository.findProjectIdById(11L)).thenReturn(Optional.of(55L));
        when(budgetCategoryRepository.findProjectIdById(12L)).thenReturn(Optional.of(55L));
        doThrow(new BusinessException("Status da empresa contratada nao permite novos vinculos financeiros."))
                .when(projectCompanyFinancialValidationService)
                .validateCanReceivePayment(eq(55L), eq(500L), any(BigDecimal.class), isNull());

        assertThatThrownBy(() -> service.createExpense(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Status da empresa contratada nao permite novos vinculos financeiros.");

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createExpense_shouldRejectWhenProjectCompanyIsInactive() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO(
                55L,
                11L,
                12L,
                null,
                LocalDate.of(2026, 3, 17),
                1,
                new BigDecimal("120.00"),
                ExpensePaymentStatusEnum.PAGO,
                ExpensePaidByEnum.INNOVATIS,
                null,
                500L,
                null,
                "Empresa inativa",
                null,
                null,
                null,
                7L
        );

        ProjectCompany projectCompany = projectCompany(500L, 55L, ContractingStatusEnum.CONTRATADA, false);

        Expense mappedExpense = new Expense();
        mappedExpense.setAmount(new BigDecimal("120.00"));
        when(expenseMapper.toEntity(dto)).thenReturn(mappedExpense);
        when(projectRepository.getReferenceById(55L)).thenReturn(project(55L));
        when(budgetItemRepository.getReferenceById(11L)).thenReturn(budgetItem(11L));
        when(budgetCategoryRepository.getReferenceById(12L)).thenReturn(category(12L));
        when(projectCompanyRepository.getReferenceById(500L)).thenReturn(projectCompany);
        when(projectCompanyRepository.findProjectIdById(500L)).thenReturn(Optional.of(55L));
        when(budgetItemRepository.findProjectIdById(11L)).thenReturn(Optional.of(55L));
        when(budgetCategoryRepository.findProjectIdById(12L)).thenReturn(Optional.of(55L));
        doThrow(new BusinessException("Empresa contratada inativa nao pode receber vinculos financeiros."))
                .when(projectCompanyFinancialValidationService)
                .validateCanReceivePayment(eq(55L), eq(500L), any(BigDecimal.class), isNull());

        assertThatThrownBy(() -> service.createExpense(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Empresa contratada inativa nao pode receber vinculos financeiros.");

        verify(expenseRepository, never()).save(any(Expense.class));
    }

    private Project project(Long id) {
        Project project = new Project();
        project.setId(id);
        return project;
    }

    private BudgetItem budgetItem(Long id) {
        BudgetItem budgetItem = new BudgetItem();
        budgetItem.setId(id);
        return budgetItem;
    }

    private BudgetCategory category(Long id) {
        BudgetCategory category = new BudgetCategory();
        category.setId(id);
        return category;
    }

    private ExpenseResponseDTO toDto(Expense expense) {
        return new ExpenseResponseDTO(
                expense.getId(),
                expense.getProject() != null ? expense.getProject().getId() : null,
                expense.getBudgetItem() != null ? expense.getBudgetItem().getId() : null,
                expense.getCategory() != null ? expense.getCategory().getId() : null,
                expense.getIncome() != null ? expense.getIncome().getId() : null,
                expense.getExpenseDate(),
                expense.getQuantity(),
                expense.getAmount(),
                expense.getPaymentStatus(),
                expense.getPaidBy() != null ? expense.getPaidBy() : ExpensePaidByEnum.INNOVATIS,
                expense.getPerson() != null ? expense.getPerson().getId() : null,
                expense.getProjectCompany() != null ? expense.getProjectCompany().getId() : null,
                expense.getOrganization() != null ? expense.getOrganization().getId() : null,
                expense.getDescription(),
                expense.getInvoiceNumber(),
                expense.getInvoiceDate(),
                expense.getDocument() != null ? expense.getDocument().getId() : null,
                expense.getIsActive(),
                expense.getCreatedAt(),
                expense.getUpdatedAt(),
                expense.getCreatedBy(),
                expense.getUpdatedBy()
        );
    }

    private ProjectCompany projectCompany(Long id, Long projectId, ContractingStatusEnum status, boolean active) {
        ProjectCompany projectCompany = new ProjectCompany();
        projectCompany.setId(id);
        projectCompany.setProject(project(projectId));
        projectCompany.setStatus(status);
        projectCompany.setIsActive(active);
        projectCompany.setTotalValue(new BigDecimal("1000.00"));
        return projectCompany;
    }
}
