package br.com.gopro.api.service.audit;

import br.com.gopro.api.model.AuditLog;
import br.com.gopro.api.model.BudgetCategory;
import br.com.gopro.api.model.BudgetItem;
import br.com.gopro.api.model.Company;
import br.com.gopro.api.model.Document;
import br.com.gopro.api.model.Goal;
import br.com.gopro.api.model.Income;
import br.com.gopro.api.model.Organization;
import br.com.gopro.api.model.Partner;
import br.com.gopro.api.model.People;
import br.com.gopro.api.model.Phase;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.model.ProjectCompany;
import br.com.gopro.api.model.ProjectPeople;
import br.com.gopro.api.model.PublicAgency;
import br.com.gopro.api.model.Secretary;
import br.com.gopro.api.model.Stage;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContractAuditChangeEnricher {

    private static final String BUDGET_ITEM_META_IDS_NOTES_PREFIX = "[[GOPRO_META_IDS:";
    private static final String BUDGET_ITEM_META_IDS_NOTES_SUFFIX = "]]";
    private static final String EMPTY_PAYMENT_LINK_LABEL = "Sem v\u00EDnculo";
    private static final String EMPTY_GOAL_LINK_LABEL = "Sem v\u00EDnculo com metas";
    private static final String EMPTY_GOAL_FINANCIAL_VALUE_LABEL = "Sem valor financeiro";
    private static final String EMPTY_NOTES_LABEL = "Sem observa\u00E7\u00F5es";
    private static final String BUDGET_ITEM_GOALS_LABEL = "Metas vinculadas";
    private static final String BUDGET_ITEM_NOTES_AND_GOALS_LABEL = "Observa\u00E7\u00F5es e metas vinculadas";

    private static final TypeReference<List<LinkedHashMap<String, Object>>> CHANGE_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<LinkedHashMap<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private static final Set<String> SUPPORTED_RESOURCES = Set.of(
            "projects",
            "documents",
            "budget-items",
            "budget-transfers",
            "disbursement-schedules",
            "stages",
            "phases",
            "expenses",
            "project-people",
            "project-companies",
            "project-organizations",
            "project_organization"
    );

    private static final Map<String, String> DISPLAY_LABELS = Map.ofEntries(
            Map.entry("Codigo do projeto", "Código do projeto"),
            Map.entry("Parceiro primario", "Parceiro primário"),
            Map.entry("Parceiro secundario", "Parceiro secundário"),
            Map.entry("Cliente primario", "Cliente primário"),
            Map.entry("Cliente secundario", "Cliente secundário"),
            Map.entry("Area/segmento", "Área/segmento"),
            Map.entry("Organizacao", "Organização")
    );

    private static final Map<String, String> PROJECT_STATUS_LABELS = Map.of(
            "PRE_PROJETO", "Pre-projeto",
            "EXECUCAO", "Execução",
            "FINALIZADO", "Finalizado",
            "SUSPENSO", "Suspenso",
            "PLANEJAMENTO", "Planejamento"
    );

    private static final Map<String, String> PROJECT_TYPE_LABELS = Map.of(
            "PROJETO", "Projeto",
            "PRODUTO", "Produto"
    );

    private static final Map<String, String> PROJECT_GOV_IF_LABELS = Map.of(
            "GOV", "GOV",
            "IF", "IF"
    );

    private static final Map<String, String> ROLE_PROJECT_PEOPLE_LABELS = Map.of(
            "DIRETOR", "Diretor",
            "BOLSISTA", "Bolsista"
    );

    private static final Map<String, String> STATUS_PROJECT_PEOPLE_LABELS = Map.of(
            "PENDENTE", "Pendente",
            "ATIVO", "Ativo",
            "ENCERRADO", "Encerrado"
    );

    private static final Map<String, String> CONTRACT_TYPE_LABELS = Map.of(
            "BOLSA", "Bolsa",
            "RPA", "RPA",
            "CLT", "CLT"
    );

    private static final Map<String, String> EXPENSE_PAYMENT_STATUS_LABELS = Map.of(
            "PAGO", "Pago",
            "RESERVADO", "Reservado"
    );

    private static final Map<String, String> EXPENSE_PAID_BY_LABELS = Map.ofEntries(
            Map.entry("INNOVATIS", "Innovatis"),
            Map.entry("EXECUCAO", "Execução"),
            Map.entry("EMPRESA", "Innovatis"),
            Map.entry("PARCEIRO", "Execução")
    );

    private static final Map<String, String> DISBURSEMENT_STATUS_LABELS = Map.of(
            "PREVISTO", "Previsto",
            "PARCIAL", "Parcial",
            "RECEBIDO", "Recebido",
            "CANCELADO", "Cancelado"
    );

    private static final Map<String, String> BUDGET_TRANSFER_STATUS_LABELS = Map.of(
            "APROVADO", "Aprovado",
            "REPEROVADO", "Reprovado"
    );

    private static final Map<String, String> DOCUMENT_STATUS_LABELS = Map.of(
            "UPLOADING", "Em envio",
            "AVAILABLE", "Disponível",
            "DELETED", "Excluído"
    );

    private static final Map<String, String> DOCUMENT_OWNER_TYPE_LABELS = Map.ofEntries(
            Map.entry("PROJECT", "Contrato"),
            Map.entry("EXPENSE", "Despesa"),
            Map.entry("BUDGET_ITEM", "Item de rubrica"),
            Map.entry("BUDGET_TRANSFER", "Remanejamento"),
            Map.entry("INCOME", "Receita"),
            Map.entry("GOAL", "Meta"),
            Map.entry("STAGE", "Etapa"),
            Map.entry("PHASE", "Fase"),
            Map.entry("PROJECT_PEOPLE", "Pessoa vinculada"),
            Map.entry("PROJECT_COMPANY", "Empresa vinculada"),
            Map.entry("PARTNER", "Parceiro"),
            Map.entry("PUBLIC_AGENCY", "Cliente"),
            Map.entry("SECRETARY", "Secretaria"),
            Map.entry("PEOPLE", "Pessoa"),
            Map.entry("ORGANIZATION", "Organização"),
            Map.entry("COMPANY", "Empresa"),
            Map.entry("USER", "Usuário")
    );

    private final ObjectMapper objectMapper;
    private final ProjectRepository projectRepository;
    private final PartnerRepository partnerRepository;
    private final PublicAgencyRepository publicAgencyRepository;
    private final SecretaryRepository secretaryRepository;
    private final PeopleRepository peopleRepository;
    private final CompanyRepository companyRepository;
    private final ProjectPeopleRepository projectPeopleRepository;
    private final ProjectCompanyRepository projectCompanyRepository;
    private final OrganizationRepository organizationRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final GoalRepository goalRepository;
    private final StageRepository stageRepository;
    private final PhaseRepository phaseRepository;
    private final IncomeRepository incomeRepository;
    private final DocumentRepository documentRepository;

    public Map<Long, String> enrich(List<AuditLog> logs, Map<Long, Project> knownProjectsById) {
        if (logs == null || logs.isEmpty()) {
            return Map.of();
        }

        Map<Long, Project> safeProjects = knownProjectsById == null ? Map.of() : knownProjectsById;
        LookupPlan lookupPlan = new LookupPlan();
        List<ParsedLog> parsedLogs = new ArrayList<>();

        for (AuditLog log : logs) {
            if (log == null || log.getId() == null) {
                continue;
            }

            List<LinkedHashMap<String, Object>> changes = parseChanges(log.getAlteracoesJson());
            if (changes.isEmpty()) {
                continue;
            }

            Map<String, Object> technical = parseMap(log.getDetalhesTecnicosJson());
            String resource = resolveResource(log, technical);
            if (resource == null || !SUPPORTED_RESOURCES.contains(resource)) {
                continue;
            }

            ParsedLog parsedLog = new ParsedLog(
                    log.getId(),
                    resource,
                    technical,
                    parseMap(log.getBeforeJson()),
                    parseMap(log.getAfterJson()),
                    changes
            );
            parsedLogs.add(parsedLog);
            collectLookupIds(parsedLog, lookupPlan, safeProjects);
        }

        if (parsedLogs.isEmpty()) {
            return Map.of();
        }

        LookupCatalog catalog = loadCatalog(lookupPlan, safeProjects);
        Map<Long, String> enrichedChangesByAuditId = new LinkedHashMap<>();

        for (ParsedLog parsedLog : parsedLogs) {
            boolean updated = false;
            for (LinkedHashMap<String, Object> change : parsedLog.changes()) {
                if (enrichChange(parsedLog, change, catalog)) {
                    updated = true;
                }
            }
            if (updated) {
                enrichedChangesByAuditId.put(parsedLog.auditLogId(), toJson(parsedLog.changes()));
            }
        }

        return enrichedChangesByAuditId;
    }

    private void collectLookupIds(ParsedLog parsedLog, LookupPlan lookupPlan, Map<Long, Project> safeProjects) {
        for (Map<String, Object> change : parsedLog.changes()) {
            String path = normalizeToken(readString(change.get("caminho")));
            if (path == null) {
                continue;
            }

            switch (parsedLog.resource()) {
                case "projects" -> collectProjectIds(path, change, lookupPlan);
                case "documents" -> collectDocumentIds(parsedLog, path, change, lookupPlan, safeProjects);
                case "budget-items" -> collectBudgetItemIds(path, change, lookupPlan);
                case "budget-transfers" -> collectBudgetTransferIds(path, change, lookupPlan);
                case "stages" -> {
                    if ("meta".equals(path)) {
                        collectLongValues(change, lookupPlan.goalIds);
                    }
                }
                case "phases" -> {
                    if ("etapa".equals(path)) {
                        collectLongValues(change, lookupPlan.stageIds);
                    }
                }
                case "expenses" -> collectExpenseIds(path, change, lookupPlan);
                case "project-people" -> {
                    if ("pessoa".equals(path)) {
                        collectLongValues(change, lookupPlan.peopleIds);
                    }
                }
                case "project-companies", "project-organizations", "project_organization" -> {
                    if ("empresa".equals(path) || "organizacao".equals(path)) {
                        collectLongValues(change, lookupPlan.companyIds);
                    }
                }
                default -> {
                    // nothing to collect
                }
            }
        }
    }

    private void collectProjectIds(String path, Map<String, Object> change, LookupPlan lookupPlan) {
        switch (path) {
            case "parceiro primario", "parceiro secundario" -> collectLongValues(change, lookupPlan.partnerIds);
            case "cliente primario" -> collectLongValues(change, lookupPlan.publicAgencyIds);
            case "cliente secundario" -> collectLongValues(change, lookupPlan.secretaryIds);
            case "coordenador" -> collectLongValues(change, lookupPlan.peopleIds);
            default -> {
                // ignore
            }
        }
    }

    private void collectDocumentIds(
            ParsedLog parsedLog,
            String path,
            Map<String, Object> change,
            LookupPlan lookupPlan,
            Map<Long, Project> safeProjects
    ) {
        if (!"dono".equals(path)) {
            return;
        }

        collectDocumentOwnerValue(change.get("de"), resolveDocumentOwnerType(parsedLog.beforeSnapshot(), parsedLog.technical()), lookupPlan, safeProjects);
        collectDocumentOwnerValue(change.get("para"), resolveDocumentOwnerType(parsedLog.afterSnapshot(), parsedLog.technical()), lookupPlan, safeProjects);
    }

    private void collectBudgetItemIds(String path, Map<String, Object> change, LookupPlan lookupPlan) {
        switch (path) {
            case "rubrica" -> collectLongValues(change, lookupPlan.budgetCategoryIds);
            case "meta" -> collectLongValues(change, lookupPlan.goalIds);
            case "observacoes" -> collectBudgetItemGoalIdsFromNotes(change, lookupPlan.goalIds);
            default -> {
                // ignore
            }
        }
    }

    private void collectBudgetTransferIds(String path, Map<String, Object> change, LookupPlan lookupPlan) {
        switch (path) {
            case "item de origem", "item de destino" -> collectLongValues(change, lookupPlan.budgetItemIds);
            case "documento" -> collectUuidValues(change, lookupPlan.documentIds);
            default -> {
                // ignore
            }
        }
    }

    private void collectExpenseIds(String path, Map<String, Object> change, LookupPlan lookupPlan) {
        switch (path) {
            case "item de rubrica" -> collectLongValues(change, lookupPlan.budgetItemIds);
            case "rubrica" -> collectLongValues(change, lookupPlan.budgetCategoryIds);
            case "receita" -> collectLongValues(change, lookupPlan.incomeIds);
            case "pessoa", "pessoa vinculada" -> collectLongValues(change, lookupPlan.peopleIds);
            case "organizacao", "organizacao vinculada", "empresa", "empresa vinculada" ->
                    collectLongValues(change, lookupPlan.organizationIds);
            case "documento" -> collectUuidValues(change, lookupPlan.documentIds);
            default -> {
                // ignore
            }
        }
    }

    private void collectBudgetItemGoalIdsFromNotes(Map<String, Object> change, Set<Long> target) {
        addBudgetItemGoalIds(change.get("de"), target);
        addBudgetItemGoalIds(change.get("para"), target);
    }

    private void addBudgetItemGoalIds(Object rawValue, Set<Long> target) {
        for (Long goalId : parseBudgetItemNotes(rawValue).goalIds()) {
            if (goalId != null) {
                target.add(goalId);
            }
        }
    }

    private void collectDocumentOwnerValue(
            Object rawValue,
            String ownerType,
            LookupPlan lookupPlan,
            Map<Long, Project> safeProjects
    ) {
        Long value = parseLong(rawValue);
        String normalizedOwnerType = normalizeToken(ownerType);
        if (value == null || normalizedOwnerType == null) {
            return;
        }

        switch (normalizedOwnerType) {
            case "project" -> {
                if (!safeProjects.containsKey(value)) {
                    lookupPlan.projectIds.add(value);
                }
            }
            case "partner" -> lookupPlan.partnerIds.add(value);
            case "public_agency" -> lookupPlan.publicAgencyIds.add(value);
            case "secretary" -> lookupPlan.secretaryIds.add(value);
            case "people" -> lookupPlan.peopleIds.add(value);
            case "company" -> lookupPlan.companyIds.add(value);
            case "project_people" -> lookupPlan.projectPeopleIds.add(value);
            case "project_company" -> lookupPlan.projectCompanyIds.add(value);
            case "organization" -> lookupPlan.organizationIds.add(value);
            case "budget_item" -> lookupPlan.budgetItemIds.add(value);
            case "goal" -> lookupPlan.goalIds.add(value);
            case "stage" -> lookupPlan.stageIds.add(value);
            case "phase" -> lookupPlan.phaseIds.add(value);
            case "income" -> lookupPlan.incomeIds.add(value);
            default -> {
                // keep technical value
            }
        }
    }

    private LookupCatalog loadCatalog(LookupPlan lookupPlan, Map<Long, Project> safeProjects) {
        Map<Long, String> projectLabels = new LinkedHashMap<>();
        for (Map.Entry<Long, Project> entry : safeProjects.entrySet()) {
            putIfPresent(projectLabels, entry.getKey(), formatProjectLabel(entry.getValue()));
        }
        putAll(projectLabels, projectRepository.findAllById(lookupPlan.projectIds).stream()
                .collect(LinkedHashMap::new, (map, project) -> putIfPresent(map, project.getId(), formatProjectLabel(project)), Map::putAll));

        return new LookupCatalog(
                projectLabels,
                loadPartnerLabels(lookupPlan.partnerIds),
                loadPublicAgencyLabels(lookupPlan.publicAgencyIds),
                loadSecretaryLabels(lookupPlan.secretaryIds),
                loadPeopleLabels(lookupPlan.peopleIds),
                loadCompanyLabels(lookupPlan.companyIds),
                loadProjectPeopleOwnerLabels(lookupPlan.projectPeopleIds),
                loadProjectCompanyOwnerLabels(lookupPlan.projectCompanyIds),
                loadOrganizationLabels(lookupPlan.organizationIds),
                loadBudgetCategoryLabels(lookupPlan.budgetCategoryIds),
                loadBudgetItemLabels(lookupPlan.budgetItemIds),
                loadGoalLabels(lookupPlan.goalIds),
                loadStageLabels(lookupPlan.stageIds),
                loadPhaseLabels(lookupPlan.phaseIds),
                loadIncomeLabels(lookupPlan.incomeIds),
                loadDocumentLabels(lookupPlan.documentIds)
        );
    }

    private boolean enrichChange(ParsedLog parsedLog, Map<String, Object> change, LookupCatalog catalog) {
        Boolean specialHandling = enrichBudgetItemNotesChange(parsedLog, change, catalog);
        if (specialHandling != null) {
            return specialHandling;
        }

        boolean updated = false;

        String path = readString(change.get("caminho"));
        String label = DISPLAY_LABELS.get(path);
        if (label != null && !Objects.equals(label, path)) {
            change.put("label", label);
            updated = true;
        }

        String beforeLabel = resolveFriendlyValue(parsedLog, change, change.get("de"), true, catalog);
        if (beforeLabel != null && !Objects.equals(beforeLabel, stringify(change.get("de")))) {
            change.put("deLabel", beforeLabel);
            updated = true;
        }

        String afterLabel = resolveFriendlyValue(parsedLog, change, change.get("para"), false, catalog);
        if (afterLabel != null && !Objects.equals(afterLabel, stringify(change.get("para")))) {
            change.put("paraLabel", afterLabel);
            updated = true;
        }

        return updated;
    }

    private Boolean enrichBudgetItemNotesChange(
            ParsedLog parsedLog,
            Map<String, Object> change,
            LookupCatalog catalog
    ) {
        if (!"budget-items".equals(parsedLog.resource())) {
            return null;
        }

        String path = normalizeToken(readString(change.get("caminho")));
        if (!"observacoes".equals(path)) {
            return null;
        }

        BudgetItemNotesSnapshot before = parseBudgetItemNotes(change.get("de"));
        BudgetItemNotesSnapshot after = parseBudgetItemNotes(change.get("para"));
        if (!before.hasGoalMetadata() && !after.hasGoalMetadata()) {
            return null;
        }

        boolean metaChanged = !Objects.equals(before.goalIds(), after.goalIds());
        boolean notesChanged = !Objects.equals(before.cleanedNotes(), after.cleanedNotes());
        boolean updated = false;

        if (metaChanged && notesChanged) {
            updated |= putDisplayValue(change, "label", BUDGET_ITEM_NOTES_AND_GOALS_LABEL);
            updated |= putDisplayValue(change, "deLabel", formatBudgetItemNotesSummary(before, catalog));
            updated |= putDisplayValue(change, "paraLabel", formatBudgetItemNotesSummary(after, catalog));
            return updated;
        }

        if (metaChanged) {
            updated |= putDisplayValue(change, "label", BUDGET_ITEM_GOALS_LABEL);
            updated |= putDisplayValue(change, "deLabel", formatGoalSelectionLabel(before.goalIds(), catalog));
            updated |= putDisplayValue(change, "paraLabel", formatGoalSelectionLabel(after.goalIds(), catalog));
            return updated;
        }

        updated |= putDisplayValue(change, "deLabel", formatNotesLabel(before.cleanedNotes()));
        updated |= putDisplayValue(change, "paraLabel", formatNotesLabel(after.cleanedNotes()));
        return updated;
    }

    private String resolveFriendlyValue(
            ParsedLog parsedLog,
            Map<String, Object> change,
            Object rawValue,
            boolean before,
            LookupCatalog catalog
    ) {
        String path = normalizeToken(readString(change.get("caminho")));
        if (path == null) {
            if (rawValue == null) {
                return null;
            }
            return resolveGenericValue(rawValue);
        }
        if (rawValue == null) {
            return resolveEmptyValueLabel(parsedLog.resource(), path);
        }

        return switch (parsedLog.resource()) {
            case "projects" -> firstNonBlank(
                    switch (path) {
                        case "parceiro primario", "parceiro secundario" -> lookupLong(catalog.partnerLabels(), rawValue);
                        case "cliente primario" -> lookupLong(catalog.publicAgencyLabels(), rawValue);
                        case "cliente secundario" -> lookupLong(catalog.secretaryLabels(), rawValue);
                        case "coordenador" -> lookupLong(catalog.peopleLabels(), rawValue);
                        case "unidade gov/if" -> lookupEnum(PROJECT_GOV_IF_LABELS, rawValue);
                        case "tipo do projeto" -> lookupEnum(PROJECT_TYPE_LABELS, rawValue);
                        case "status do projeto" -> lookupEnum(PROJECT_STATUS_LABELS, rawValue);
                        default -> null;
                    },
                    resolveGenericValue(rawValue)
            );
            case "documents" -> firstNonBlank(
                    switch (path) {
                        case "tipo do dono" -> lookupEnum(DOCUMENT_OWNER_TYPE_LABELS, rawValue);
                        case "dono" -> resolveDocumentOwnerLabel(parsedLog, rawValue, before, catalog);
                        case "status do arquivo" -> lookupEnum(DOCUMENT_STATUS_LABELS, rawValue);
                        default -> null;
                    },
                    resolveGenericValue(rawValue)
            );
            case "budget-items" -> firstNonBlank(
                    switch (path) {
                        case "rubrica" -> lookupLong(catalog.budgetCategoryLabels(), rawValue);
                        case "meta" -> lookupLong(catalog.goalLabels(), rawValue);
                        case "metas vinculadas" -> formatGoalSelectionLabel(parseBudgetItemNotes(rawValue).goalIds(), catalog);
                        default -> null;
                    },
                    resolveGenericValue(rawValue)
            );
            case "budget-transfers" -> firstNonBlank(
                    switch (path) {
                        case "item de origem", "item de destino" -> lookupLong(catalog.budgetItemLabels(), rawValue);
                        case "documento" -> lookupUuid(catalog.documentLabels(), rawValue);
                        case "status do remanejamento" -> lookupEnum(BUDGET_TRANSFER_STATUS_LABELS, rawValue);
                        default -> null;
                    },
                    resolveGenericValue(rawValue)
            );
            case "disbursement-schedules" -> firstNonBlank(
                    "status do desembolso".equals(path) ? lookupEnum(DISBURSEMENT_STATUS_LABELS, rawValue) : null,
                    resolveGenericValue(rawValue)
            );
            case "stages" -> firstNonBlank("meta".equals(path) ? lookupLong(catalog.goalLabels(), rawValue) : null, resolveGenericValue(rawValue));
            case "phases" -> firstNonBlank("etapa".equals(path) ? lookupLong(catalog.stageLabels(), rawValue) : null, resolveGenericValue(rawValue));
            case "expenses" -> firstNonBlank(
                    switch (path) {
                        case "item de rubrica" -> lookupLong(catalog.budgetItemLabels(), rawValue);
                        case "rubrica" -> lookupLong(catalog.budgetCategoryLabels(), rawValue);
                        case "receita" -> lookupLong(catalog.incomeLabels(), rawValue);
                        case "status do pagamento" -> lookupEnum(EXPENSE_PAYMENT_STATUS_LABELS, rawValue);
                        case "realizado por" -> lookupEnum(EXPENSE_PAID_BY_LABELS, rawValue);
                        case "pessoa", "pessoa vinculada" -> lookupLong(catalog.peopleLabels(), rawValue);
                        case "organizacao", "organizacao vinculada", "empresa", "empresa vinculada" ->
                                lookupLong(catalog.organizationLabels(), rawValue);
                        case "documento" -> lookupUuid(catalog.documentLabels(), rawValue);
                        default -> null;
                    },
                    resolveGenericValue(rawValue)
            );
            case "project-people" -> firstNonBlank(
                    switch (path) {
                        case "pessoa" -> lookupLong(catalog.peopleLabels(), rawValue);
                        case "papel" -> lookupEnum(ROLE_PROJECT_PEOPLE_LABELS, rawValue);
                        case "tipo de contrato" -> lookupEnum(CONTRACT_TYPE_LABELS, rawValue);
                        case "status" -> lookupEnum(STATUS_PROJECT_PEOPLE_LABELS, rawValue);
                        default -> null;
                    },
                    resolveGenericValue(rawValue)
            );
            case "project-companies", "project-organizations", "project_organization" -> firstNonBlank(
                    ("empresa".equals(path) || "organizacao".equals(path)) ? lookupLong(catalog.companyLabels(), rawValue) : null,
                    resolveGenericValue(rawValue)
            );
            default -> resolveGenericValue(rawValue);
        };
    }

    private String resolveDocumentOwnerLabel(ParsedLog parsedLog, Object rawValue, boolean before, LookupCatalog catalog) {
        String ownerType = before
                ? resolveDocumentOwnerType(parsedLog.beforeSnapshot(), parsedLog.technical())
                : resolveDocumentOwnerType(parsedLog.afterSnapshot(), parsedLog.technical());
        String normalizedOwnerType = normalizeToken(ownerType);
        if (normalizedOwnerType == null) {
            return null;
        }

        return switch (normalizedOwnerType) {
            case "project" -> lookupLong(catalog.projectLabels(), rawValue);
            case "partner" -> lookupLong(catalog.partnerLabels(), rawValue);
            case "public_agency" -> lookupLong(catalog.publicAgencyLabels(), rawValue);
            case "secretary" -> lookupLong(catalog.secretaryLabels(), rawValue);
            case "people" -> lookupLong(catalog.peopleLabels(), rawValue);
            case "company" -> lookupLong(catalog.companyLabels(), rawValue);
            case "project_people" -> lookupLong(catalog.projectPeopleOwnerLabels(), rawValue);
            case "project_company" -> lookupLong(catalog.projectCompanyOwnerLabels(), rawValue);
            case "organization" -> lookupLong(catalog.organizationLabels(), rawValue);
            case "budget_item" -> lookupLong(catalog.budgetItemLabels(), rawValue);
            case "goal" -> lookupLong(catalog.goalLabels(), rawValue);
            case "stage" -> lookupLong(catalog.stageLabels(), rawValue);
            case "phase" -> lookupLong(catalog.phaseLabels(), rawValue);
            case "income" -> lookupLong(catalog.incomeLabels(), rawValue);
            default -> null;
        };
    }

    private String resolveGenericValue(Object rawValue) {
        if (rawValue instanceof Boolean bool) {
            return bool ? "Sim" : "Não";
        }

        String text = readString(rawValue);
        if (text == null) {
            return null;
        }
        if ("true".equalsIgnoreCase(text)) {
            return "Sim";
        }
        if ("false".equalsIgnoreCase(text)) {
            return "Não";
        }
        return null;
    }

    private String resolveEmptyValueLabel(String resource, String path) {
        if (resource == null || path == null) {
            return null;
        }

        return switch (resource) {
            case "expenses" -> switch (path) {
                case "pessoa", "pessoa vinculada", "organizacao", "organizacao vinculada", "empresa", "empresa vinculada" ->
                        EMPTY_PAYMENT_LINK_LABEL;
                default -> null;
            };
            case "budget-items" -> switch (path) {
                case "meta", "metas vinculadas" -> EMPTY_GOAL_LINK_LABEL;
                case "observacoes" -> EMPTY_NOTES_LABEL;
                default -> null;
            };
            case "goals" -> "valor financeiro da meta".equals(path) ? EMPTY_GOAL_FINANCIAL_VALUE_LABEL : null;
            case "stages" -> "valor financeiro da etapa".equals(path) ? EMPTY_GOAL_FINANCIAL_VALUE_LABEL : null;
            default -> null;
        };
    }

    private BudgetItemNotesSnapshot parseBudgetItemNotes(Object rawValue) {
        String notes = readString(rawValue);
        if (notes == null) {
            return new BudgetItemNotesSnapshot(List.of(), null, false);
        }

        List<String> lines = new ArrayList<>();
        for (String line : notes.split("\\r?\\n")) {
            String trimmed = trimToNull(line);
            if (trimmed != null) {
                lines.add(trimmed);
            }
        }

        Set<Long> goalIds = new LinkedHashSet<>();
        List<String> remainingLines = new ArrayList<>();
        boolean hasGoalMetadata = false;

        for (String line : lines) {
            if (line.startsWith(BUDGET_ITEM_META_IDS_NOTES_PREFIX) && line.endsWith(BUDGET_ITEM_META_IDS_NOTES_SUFFIX)) {
                hasGoalMetadata = true;
                String serializedIds = line.substring(
                        BUDGET_ITEM_META_IDS_NOTES_PREFIX.length(),
                        line.length() - BUDGET_ITEM_META_IDS_NOTES_SUFFIX.length()
                );
                for (String value : serializedIds.split(",")) {
                    Long goalId = parseLong(value);
                    if (goalId != null) {
                        goalIds.add(goalId);
                    }
                }
                continue;
            }
            remainingLines.add(line);
        }

        return new BudgetItemNotesSnapshot(
                List.copyOf(goalIds),
                remainingLines.isEmpty() ? null : String.join("\n", remainingLines),
                hasGoalMetadata
        );
    }

    private String formatGoalSelectionLabel(List<Long> goalIds, LookupCatalog catalog) {
        if (goalIds == null || goalIds.isEmpty()) {
            return EMPTY_GOAL_LINK_LABEL;
        }

        List<String> labels = new ArrayList<>();
        for (Long goalId : goalIds) {
            if (goalId == null) {
                continue;
            }
            labels.add(firstNonBlank(catalog.goalLabels().get(goalId), "Meta #" + goalId));
        }
        return labels.isEmpty() ? EMPTY_GOAL_LINK_LABEL : String.join(", ", labels);
    }

    private String formatBudgetItemNotesSummary(BudgetItemNotesSnapshot snapshot, LookupCatalog catalog) {
        return "Metas: "
                + formatGoalSelectionLabel(snapshot.goalIds(), catalog)
                + " | Observa\u00E7\u00F5es: "
                + formatNotesLabel(snapshot.cleanedNotes());
    }

    private String formatNotesLabel(String notes) {
        return firstNonBlank(trimToNull(notes), EMPTY_NOTES_LABEL);
    }

    private boolean putDisplayValue(Map<String, Object> change, String key, String value) {
        Object current = change.get(key);
        if (value == null) {
            return change.remove(key) != null;
        }
        if (Objects.equals(current, value)) {
            return false;
        }
        change.put(key, value);
        return true;
    }

    private Map<Long, String> loadPartnerLabels(Set<Long> ids) {
        return loadLongLabels(partnerRepository.findAllById(ids), Partner::getId, partner -> firstNonBlank(partner.getTradeName(), partner.getName(), partner.getAcronym()));
    }

    private Map<Long, String> loadPublicAgencyLabels(Set<Long> ids) {
        return loadLongLabels(publicAgencyRepository.findAllById(ids), PublicAgency::getId, agency -> composeName(agency.getSigla(), agency.getName(), agency.getCode()));
    }

    private Map<Long, String> loadSecretaryLabels(Set<Long> ids) {
        return loadLongLabels(secretaryRepository.findAllById(ids), Secretary::getId, secretary -> composeName(secretary.getSigla(), secretary.getName(), secretary.getCode()));
    }

    private Map<Long, String> loadPeopleLabels(Set<Long> ids) {
        return loadLongLabels(peopleRepository.findAllById(ids), People::getId, people -> firstNonBlank(people.getFullName(), people.getEmail()));
    }

    private Map<Long, String> loadCompanyLabels(Set<Long> ids) {
        return loadLongLabels(companyRepository.findAllById(ids), Company::getId, company -> firstNonBlank(company.getTradeName(), company.getName()));
    }

    private Map<Long, String> loadProjectPeopleOwnerLabels(Set<Long> ids) {
        LinkedHashMap<Long, Long> personIdByOwnerId = new LinkedHashMap<>();
        Set<Long> personIds = new LinkedHashSet<>();

        for (ProjectPeople projectPeople : projectPeopleRepository.findAllById(ids)) {
            if (projectPeople == null || projectPeople.getId() == null || projectPeople.getPerson() == null) {
                continue;
            }
            Long personId = projectPeople.getPerson().getId();
            if (personId == null) {
                continue;
            }
            personIdByOwnerId.put(projectPeople.getId(), personId);
            personIds.add(personId);
        }

        Map<Long, String> peopleLabels = personIds.isEmpty() ? Map.of() : loadPeopleLabels(personIds);
        LinkedHashMap<Long, String> labels = new LinkedHashMap<>();
        for (Map.Entry<Long, Long> entry : personIdByOwnerId.entrySet()) {
            putIfPresent(labels, entry.getKey(), peopleLabels.get(entry.getValue()));
        }
        return labels;
    }

    private Map<Long, String> loadProjectCompanyOwnerLabels(Set<Long> ids) {
        LinkedHashMap<Long, Long> companyIdByOwnerId = new LinkedHashMap<>();
        Set<Long> companyIds = new LinkedHashSet<>();

        for (ProjectCompany projectCompany : projectCompanyRepository.findAllById(ids)) {
            if (projectCompany == null || projectCompany.getId() == null || projectCompany.getCompany() == null) {
                continue;
            }
            Long companyId = projectCompany.getCompany().getId();
            if (companyId == null) {
                continue;
            }
            companyIdByOwnerId.put(projectCompany.getId(), companyId);
            companyIds.add(companyId);
        }

        Map<Long, String> companyLabels = companyIds.isEmpty() ? Map.of() : loadCompanyLabels(companyIds);
        LinkedHashMap<Long, String> labels = new LinkedHashMap<>();
        for (Map.Entry<Long, Long> entry : companyIdByOwnerId.entrySet()) {
            putIfPresent(labels, entry.getKey(), companyLabels.get(entry.getValue()));
        }
        return labels;
    }

    private Map<Long, String> loadOrganizationLabels(Set<Long> ids) {
        return loadLongLabels(organizationRepository.findAllById(ids), Organization::getId, organization -> firstNonBlank(organization.getTradeName(), organization.getName()));
    }

    private Map<Long, String> loadBudgetCategoryLabels(Set<Long> ids) {
        return loadLongLabels(budgetCategoryRepository.findAllById(ids), BudgetCategory::getId, category -> composeName(category.getCode(), category.getName(), null));
    }

    private Map<Long, String> loadBudgetItemLabels(Set<Long> ids) {
        return loadLongLabels(budgetItemRepository.findAllById(ids), BudgetItem::getId, item -> firstNonBlank(item.getDescription(), "Item de rubrica #" + item.getId()));
    }

    private Map<Long, String> loadGoalLabels(Set<Long> ids) {
        return loadLongLabels(goalRepository.findAllById(ids), Goal::getId, this::formatGoalLabel);
    }

    private Map<Long, String> loadStageLabels(Set<Long> ids) {
        return loadLongLabels(stageRepository.findAllById(ids), Stage::getId, this::formatStageLabel);
    }

    private Map<Long, String> loadPhaseLabels(Set<Long> ids) {
        return loadLongLabels(phaseRepository.findAllById(ids), Phase::getId, this::formatPhaseLabel);
    }

    private Map<Long, String> loadIncomeLabels(Set<Long> ids) {
        return loadLongLabels(incomeRepository.findAllById(ids), Income::getId, this::formatIncomeLabel);
    }

    private Map<UUID, String> loadDocumentLabels(Set<UUID> ids) {
        LinkedHashMap<UUID, String> labels = new LinkedHashMap<>();
        for (Document document : documentRepository.findAllById(ids)) {
            putIfPresent(labels, document.getId(), firstNonBlank(document.getOriginalName(), document.getId().toString()));
        }
        return labels;
    }

    private <T> Map<Long, String> loadLongLabels(Iterable<T> values, java.util.function.Function<T, Long> idResolver, java.util.function.Function<T, String> labelResolver) {
        LinkedHashMap<Long, String> labels = new LinkedHashMap<>();
        for (T value : values) {
            putIfPresent(labels, idResolver.apply(value), labelResolver.apply(value));
        }
        return labels;
    }

    private String formatProjectLabel(Project project) {
        if (project == null) {
            return null;
        }
        String code = trimToNull(project.getCode());
        String name = trimToNull(project.getName());
        return code != null && name != null && !code.equalsIgnoreCase(name) ? code + " - " + name : firstNonBlank(code, name);
    }

    private String formatGoalLabel(Goal goal) {
        if (goal == null) {
            return null;
        }
        return goal.getNumero() != null && trimToNull(goal.getTitulo()) != null
                ? "Meta " + goal.getNumero() + " - " + goal.getTitulo().trim()
                : goal.getNumero() != null ? "Meta " + goal.getNumero() : trimToNull(goal.getTitulo());
    }

    private String formatStageLabel(Stage stage) {
        if (stage == null) {
            return null;
        }
        return stage.getNumero() != null && trimToNull(stage.getTitulo()) != null
                ? "Etapa " + stage.getNumero() + " - " + stage.getTitulo().trim()
                : stage.getNumero() != null ? "Etapa " + stage.getNumero() : trimToNull(stage.getTitulo());
    }

    private String formatPhaseLabel(Phase phase) {
        if (phase == null) {
            return null;
        }
        return phase.getNumero() != null && trimToNull(phase.getTitulo()) != null
                ? "Fase " + phase.getNumero() + " - " + phase.getTitulo().trim()
                : phase.getNumero() != null ? "Fase " + phase.getNumero() : trimToNull(phase.getTitulo());
    }

    private String formatIncomeLabel(Income income) {
        if (income == null) {
            return null;
        }
        return income.getNumero() != null ? "Receita " + income.getNumero() : trimToNull(income.getInvoiceNumber());
    }

    private String lookupEnum(Map<String, String> labels, Object rawValue) {
        String normalized = trimToNull(readString(rawValue));
        return normalized == null ? null : labels.get(normalized.toUpperCase(Locale.ROOT));
    }

    private String lookupLong(Map<Long, String> labels, Object rawValue) {
        Long id = parseLong(rawValue);
        return id == null ? null : labels.get(id);
    }

    private String lookupUuid(Map<UUID, String> labels, Object rawValue) {
        UUID id = parseUuid(rawValue);
        return id == null ? null : labels.get(id);
    }

    private List<LinkedHashMap<String, Object>> parseChanges(String json) {
        try {
            List<LinkedHashMap<String, Object>> parsed = objectMapper.readValue(json, CHANGE_LIST_TYPE);
            return parsed == null ? List.of() : parsed;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private LinkedHashMap<String, Object> parseMap(String json) {
        String normalized = trimToNull(json);
        if (normalized == null) {
            return new LinkedHashMap<>();
        }
        try {
            LinkedHashMap<String, Object> parsed = objectMapper.readValue(normalized, MAP_TYPE);
            return parsed == null ? new LinkedHashMap<>() : parsed;
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private String resolveResource(AuditLog log, Map<String, Object> technical) {
        String resource = trimToNull(readString(technical.get("resource")));
        if (resource != null) {
            int dotIndex = resource.indexOf('.');
            return dotIndex < 0 ? resource : resource.substring(0, dotIndex);
        }
        return null;
    }

    private String resolveDocumentOwnerType(Map<String, Object> snapshot, Map<String, Object> technical) {
        String ownerType = snapshot == null ? null : readString(snapshot.get("ownerType"));
        if (trimToNull(ownerType) != null) {
            return ownerType;
        }
        return technical == null ? null : readString(technical.get("ownerType"));
    }

    private void collectLongValues(Map<String, Object> change, Set<Long> target) {
        addLong(change.get("de"), target);
        addLong(change.get("para"), target);
    }

    private void collectUuidValues(Map<String, Object> change, Set<UUID> target) {
        addUuid(change.get("de"), target);
        addUuid(change.get("para"), target);
    }

    private void addLong(Object rawValue, Set<Long> target) {
        Long value = parseLong(rawValue);
        if (value != null) {
            target.add(value);
        }
    }

    private void addUuid(Object rawValue, Set<UUID> target) {
        UUID value = parseUuid(rawValue);
        if (value != null) {
            target.add(value);
        }
    }

    private Long parseLong(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Number number) {
            return number.longValue();
        }
        String text = trimToNull(String.valueOf(rawValue));
        if (text == null) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private UUID parseUuid(Object rawValue) {
        String text = trimToNull(String.valueOf(rawValue));
        if (text == null) {
            return null;
        }
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String normalizeToken(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private String readString(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        String text = String.valueOf(rawValue).trim();
        return text.isEmpty() ? null : text;
    }

    private String stringify(Object rawValue) {
        return rawValue == null ? null : rawValue instanceof String text ? text : String.valueOf(rawValue);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String composeName(String code, String name, String fallback) {
        String normalizedCode = trimToNull(code);
        String normalizedName = trimToNull(name);
        return normalizedCode != null && normalizedName != null && !normalizedCode.equalsIgnoreCase(normalizedName)
                ? normalizedCode + " - " + normalizedName
                : firstNonBlank(normalizedName, normalizedCode, fallback);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = trimToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private <K> void putIfPresent(Map<K, String> target, K key, String value) {
        if (key != null && value != null && !value.isBlank()) {
            target.put(key, value.trim());
        }
    }

    private <K> void putAll(Map<K, String> target, Map<K, String> source) {
        if (source != null) {
            target.putAll(source);
        }
    }

    private record ParsedLog(
            Long auditLogId,
            String resource,
            Map<String, Object> technical,
            Map<String, Object> beforeSnapshot,
            Map<String, Object> afterSnapshot,
            List<LinkedHashMap<String, Object>> changes
    ) {
    }

    private record LookupCatalog(
            Map<Long, String> projectLabels,
            Map<Long, String> partnerLabels,
            Map<Long, String> publicAgencyLabels,
            Map<Long, String> secretaryLabels,
            Map<Long, String> peopleLabels,
            Map<Long, String> companyLabels,
            Map<Long, String> projectPeopleOwnerLabels,
            Map<Long, String> projectCompanyOwnerLabels,
            Map<Long, String> organizationLabels,
            Map<Long, String> budgetCategoryLabels,
            Map<Long, String> budgetItemLabels,
            Map<Long, String> goalLabels,
            Map<Long, String> stageLabels,
            Map<Long, String> phaseLabels,
            Map<Long, String> incomeLabels,
            Map<UUID, String> documentLabels
    ) {
    }

    private record BudgetItemNotesSnapshot(
            List<Long> goalIds,
            String cleanedNotes,
            boolean hasGoalMetadata
    ) {
    }

    private static final class LookupPlan {
        private final Set<Long> projectIds = new LinkedHashSet<>();
        private final Set<Long> partnerIds = new LinkedHashSet<>();
        private final Set<Long> publicAgencyIds = new LinkedHashSet<>();
        private final Set<Long> secretaryIds = new LinkedHashSet<>();
        private final Set<Long> peopleIds = new LinkedHashSet<>();
        private final Set<Long> companyIds = new LinkedHashSet<>();
        private final Set<Long> projectPeopleIds = new LinkedHashSet<>();
        private final Set<Long> projectCompanyIds = new LinkedHashSet<>();
        private final Set<Long> organizationIds = new LinkedHashSet<>();
        private final Set<Long> budgetCategoryIds = new LinkedHashSet<>();
        private final Set<Long> budgetItemIds = new LinkedHashSet<>();
        private final Set<Long> goalIds = new LinkedHashSet<>();
        private final Set<Long> stageIds = new LinkedHashSet<>();
        private final Set<Long> phaseIds = new LinkedHashSet<>();
        private final Set<Long> incomeIds = new LinkedHashSet<>();
        private final Set<UUID> documentIds = new LinkedHashSet<>();
    }
}
