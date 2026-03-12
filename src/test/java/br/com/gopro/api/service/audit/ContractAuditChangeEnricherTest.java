package br.com.gopro.api.service.audit;

import br.com.gopro.api.model.AuditLog;
import br.com.gopro.api.model.BudgetCategory;
import br.com.gopro.api.model.Company;
import br.com.gopro.api.model.Goal;
import br.com.gopro.api.model.Partner;
import br.com.gopro.api.model.People;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.model.ProjectCompany;
import br.com.gopro.api.model.ProjectPeople;
import br.com.gopro.api.repository.BudgetCategoryRepository;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.CompanyRepository;
import br.com.gopro.api.repository.DocumentRepository;
import br.com.gopro.api.repository.GoalRepository;
import br.com.gopro.api.repository.IncomeRepository;
import br.com.gopro.api.repository.OrganizationRepository;
import br.com.gopro.api.repository.PartnerRepository;
import br.com.gopro.api.repository.PeopleRepository;
import br.com.gopro.api.repository.PhaseRepository;
import br.com.gopro.api.repository.ProjectCompanyRepository;
import br.com.gopro.api.repository.ProjectPeopleRepository;
import br.com.gopro.api.repository.ProjectRepository;
import br.com.gopro.api.repository.PublicAgencyRepository;
import br.com.gopro.api.repository.SecretaryRepository;
import br.com.gopro.api.repository.StageRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractAuditChangeEnricherTest {

    private static final TypeReference<List<LinkedHashMap<String, Object>>> CHANGE_LIST_TYPE = new TypeReference<>() {
    };

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private PartnerRepository partnerRepository;
    @Mock
    private PublicAgencyRepository publicAgencyRepository;
    @Mock
    private SecretaryRepository secretaryRepository;
    @Mock
    private PeopleRepository peopleRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private ProjectPeopleRepository projectPeopleRepository;
    @Mock
    private ProjectCompanyRepository projectCompanyRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private BudgetCategoryRepository budgetCategoryRepository;
    @Mock
    private BudgetItemRepository budgetItemRepository;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private StageRepository stageRepository;
    @Mock
    private PhaseRepository phaseRepository;
    @Mock
    private IncomeRepository incomeRepository;
    @Mock
    private DocumentRepository documentRepository;

    private ObjectMapper objectMapper;
    private ContractAuditChangeEnricher enricher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        enricher = new ContractAuditChangeEnricher(
                objectMapper,
                projectRepository,
                partnerRepository,
                publicAgencyRepository,
                secretaryRepository,
                peopleRepository,
                companyRepository,
                projectPeopleRepository,
                projectCompanyRepository,
                organizationRepository,
                budgetCategoryRepository,
                budgetItemRepository,
                goalRepository,
                stageRepository,
                phaseRepository,
                incomeRepository,
                documentRepository
        );
    }

    @Test
    void enrich_projectsShouldAddFriendlyRelationAndEnumLabels() throws Exception {
        Partner oldPartner = new Partner();
        oldPartner.setId(12L);
        oldPartner.setTradeName("Empresa Alfa");

        Partner newPartner = new Partner();
        newPartner.setId(18L);
        newPartner.setTradeName("Empresa Beta");

        when(partnerRepository.findAllById(any())).thenReturn(List.of(oldPartner, newPartner));

        AuditLog log = auditLog(
                1L,
                "projects",
                List.of(
                        change("Parceiro primario", "12", "18"),
                        change("Status do projeto", "PLANEJAMENTO", "EXECUCAO"),
                        change("Valor do contrato", "1000", "1500")
                ),
                null,
                null
        );

        Map<Long, String> enriched = enricher.enrich(List.of(log), Map.of());
        List<LinkedHashMap<String, Object>> changes = parseChanges(enriched.get(1L));

        assertThat(changes).hasSize(3);
        assertThat(changes.get(0))
                .containsEntry("deLabel", "Empresa Alfa")
                .containsEntry("paraLabel", "Empresa Beta");
        assertThat(changes.get(1))
                .containsEntry("deLabel", "Planejamento")
                .containsEntry("paraLabel", "Execução");
        assertThat(changes.get(2)).doesNotContainKeys("deLabel", "paraLabel");
    }

    @Test
    void enrich_budgetItemsAndProjectPeopleShouldAddFriendlyBusinessLabels() throws Exception {
        BudgetCategory oldCategory = new BudgetCategory();
        oldCategory.setId(8L);
        oldCategory.setCode("RUB-1");
        oldCategory.setName("Despesas administrativas");

        BudgetCategory newCategory = new BudgetCategory();
        newCategory.setId(10L);
        newCategory.setCode("RUB-2");
        newCategory.setName("Despesas operacionais");

        Goal goal = new Goal();
        goal.setId(11L);
        goal.setNumero(3);
        goal.setTitulo("Meta expandida");

        People oldPerson = new People();
        oldPerson.setId(44L);
        oldPerson.setFullName("Maria Silva");

        People newPerson = new People();
        newPerson.setId(52L);
        newPerson.setFullName("João Souza");

        when(budgetCategoryRepository.findAllById(any())).thenReturn(List.of(oldCategory, newCategory));
        when(goalRepository.findAllById(any())).thenReturn(List.of(goal));
        when(peopleRepository.findAllById(any())).thenReturn(List.of(oldPerson, newPerson));

        AuditLog budgetItemLog = auditLog(
                2L,
                "budget-items",
                List.of(
                        change("Rubrica", "8", "10"),
                        change("Meta", null, "11")
                ),
                null,
                null
        );

        AuditLog peopleLog = auditLog(
                3L,
                "project-people",
                List.of(
                        change("Pessoa", "44", "52"),
                        change("Papel", "BOLSISTA", "DIRETOR")
                ),
                null,
                null
        );

        Map<Long, String> enriched = enricher.enrich(List.of(budgetItemLog, peopleLog), Map.of());
        List<LinkedHashMap<String, Object>> budgetChanges = parseChanges(enriched.get(2L));
        List<LinkedHashMap<String, Object>> peopleChanges = parseChanges(enriched.get(3L));

        assertThat(budgetChanges.get(0))
                .containsEntry("deLabel", "RUB-1 - Despesas administrativas")
                .containsEntry("paraLabel", "RUB-2 - Despesas operacionais");
        assertThat(budgetChanges.get(1)).containsEntry("paraLabel", "Meta 3 - Meta expandida");
        assertThat(peopleChanges.get(0))
                .containsEntry("deLabel", "Maria Silva")
                .containsEntry("paraLabel", "João Souza");
        assertThat(peopleChanges.get(1))
                .containsEntry("deLabel", "Bolsista")
                .containsEntry("paraLabel", "Diretor");
    }

    @Test
    void enrich_projectCompaniesAndDocumentsShouldUseSafeFriendlyNames() throws Exception {
        Company oldCompany = new Company();
        oldCompany.setId(3L);
        oldCompany.setTradeName("Fornecedor XPTO");

        Company newCompany = new Company();
        newCompany.setId(7L);
        newCompany.setTradeName("Cliente Y");

        when(companyRepository.findAllById(any())).thenReturn(List.of(oldCompany, newCompany));

        Project oldProject = new Project();
        oldProject.setId(15L);
        oldProject.setCode("CT-2024-015");
        oldProject.setName("Contrato A");

        Project newProject = new Project();
        newProject.setId(16L);
        newProject.setCode("CT-2024-016");
        newProject.setName("Contrato B");

        AuditLog companyLog = auditLog(
                4L,
                "project-companies",
                List.of(change("Empresa", "3", "7")),
                null,
                null
        );

        AuditLog documentLog = auditLog(
                5L,
                "documents",
                List.of(change("Dono", "15", "16")),
                Map.of("ownerType", "PROJECT"),
                Map.of("ownerType", "PROJECT")
        );

        Map<Long, String> enriched = enricher.enrich(
                List.of(companyLog, documentLog),
                Map.of(15L, oldProject, 16L, newProject)
        );

        List<LinkedHashMap<String, Object>> companyChanges = parseChanges(enriched.get(4L));
        List<LinkedHashMap<String, Object>> documentChanges = parseChanges(enriched.get(5L));

        assertThat(companyChanges.get(0))
                .containsEntry("deLabel", "Fornecedor XPTO")
                .containsEntry("paraLabel", "Cliente Y");
        assertThat(documentChanges.get(0))
                .containsEntry("deLabel", "CT-2024-015 - Contrato A")
                .containsEntry("paraLabel", "CT-2024-016 - Contrato B");
    }

    @Test
    void enrich_documentsShouldHandleLiteralNullSnapshots() throws Exception {
        Project oldProject = new Project();
        oldProject.setId(15L);
        oldProject.setCode("CT-2024-015");
        oldProject.setName("Contrato A");

        Project newProject = new Project();
        newProject.setId(16L);
        newProject.setCode("CT-2024-016");
        newProject.setName("Contrato B");

        AuditLog documentLog = auditLog(
                55L,
                "documents",
                List.of(change("Dono", "15", "16")),
                null,
                null
        );
        documentLog.setDetalhesTecnicosJson(objectMapper.writeValueAsString(Map.of(
                "resource", "documents",
                "ownerType", "PROJECT"
        )));
        documentLog.setBeforeJson("null");
        documentLog.setAfterJson("null");

        Map<Long, String> enriched = enricher.enrich(
                List.of(documentLog),
                Map.of(15L, oldProject, 16L, newProject)
        );

        List<LinkedHashMap<String, Object>> documentChanges = parseChanges(enriched.get(55L));

        assertThat(documentChanges.get(0))
                .containsEntry("deLabel", "CT-2024-015 - Contrato A")
                .containsEntry("paraLabel", "CT-2024-016 - Contrato B");
    }

    @Test
    void enrich_documentsOwnedByLinkedRecordsShouldResolveFriendlyOwnerNamesSafely() throws Exception {
        People person = new People();
        person.setId(44L);
        person.setFullName("Maria Silva");

        ProjectPeople projectPeople = new ProjectPeople();
        projectPeople.setId(400L);
        projectPeople.setPerson(person);

        Company company = new Company();
        company.setId(7L);
        company.setTradeName("Empresa Beta");

        ProjectCompany projectCompany = new ProjectCompany();
        projectCompany.setId(700L);
        projectCompany.setCompany(company);

        when(projectPeopleRepository.findAllById(any())).thenReturn(List.of(projectPeople));
        when(projectCompanyRepository.findAllById(any())).thenReturn(List.of(projectCompany));
        when(peopleRepository.findAllById(any())).thenReturn(List.of(person));
        when(companyRepository.findAllById(any())).thenReturn(List.of(company));

        AuditLog documentLog = auditLog(
                6L,
                "documents",
                List.of(
                        change("Tipo do dono", "PROJECT_PEOPLE", "PROJECT_COMPANY"),
                        change("Dono", "400", "700")
                ),
                Map.of("ownerType", "PROJECT_PEOPLE"),
                Map.of("ownerType", "PROJECT_COMPANY")
        );

        Map<Long, String> enriched = enricher.enrich(List.of(documentLog), Map.of());
        List<LinkedHashMap<String, Object>> changes = parseChanges(enriched.get(6L));

        assertThat(changes.get(0))
                .containsEntry("deLabel", "Pessoa vinculada")
                .containsEntry("paraLabel", "Empresa vinculada");
        assertThat(changes.get(1))
                .containsEntry("deLabel", "Maria Silva")
                .containsEntry("paraLabel", "Empresa Beta");
    }

    @Test
    void enrich_whenFriendlyLookupIsUnavailableShouldKeepTechnicalPayloadUntouched() throws Exception {
        when(partnerRepository.findAllById(any())).thenReturn(List.of());

        AuditLog log = auditLog(
                7L,
                "projects",
                List.of(change("Parceiro primario", "12", "18")),
                null,
                null
        );

        Map<Long, String> enriched = enricher.enrich(List.of(log), Map.of());

        List<LinkedHashMap<String, Object>> changes = parseChanges(enriched.get(7L));

        assertThat(changes).hasSize(1);
        assertThat(changes.get(0))
                .containsEntry("label", "Parceiro primário")
                .doesNotContainKeys("deLabel", "paraLabel");
    }

    private AuditLog auditLog(
            Long id,
            String resource,
            List<LinkedHashMap<String, Object>> changes,
            Map<String, Object> before,
            Map<String, Object> after
    ) throws Exception {
        AuditLog log = new AuditLog();
        log.setId(id);
        log.setEntityType("contracts:" + resource);
        log.setAlteracoesJson(objectMapper.writeValueAsString(changes));
        log.setDetalhesTecnicosJson(objectMapper.writeValueAsString(Map.of("resource", resource)));
        if (before != null) {
            log.setBeforeJson(objectMapper.writeValueAsString(before));
        }
        if (after != null) {
            log.setAfterJson(objectMapper.writeValueAsString(after));
        }
        return log;
    }

    private LinkedHashMap<String, Object> change(String caminho, String de, String para) {
        LinkedHashMap<String, Object> change = new LinkedHashMap<>();
        change.put("caminho", caminho);
        change.put("de", de);
        change.put("para", para);
        change.put("tipo", "EDITADO");
        return change;
    }

    private List<LinkedHashMap<String, Object>> parseChanges(String json) throws Exception {
        return objectMapper.readValue(json, CHANGE_LIST_TYPE);
    }
}
