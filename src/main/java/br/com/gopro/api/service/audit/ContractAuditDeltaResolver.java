package br.com.gopro.api.service.audit;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.util.Map.entry;

@Component
public class ContractAuditDeltaResolver {

    private static final Set<String> GENERIC_IGNORED_FIELDS = Set.of(
            "id",
            "createdAt",
            "updatedAt",
            "createdBy",
            "updatedBy",
            "approvedAt",
            "approvedBy",
            "deletedAt",
            "isActive"
    );

    private static final Map<String, Set<String>> RESOURCE_IGNORED_FIELDS = Map.ofEntries(
            entry("projects", Set.of("totalReceived", "totalExpenses", "saldo")),
            entry("documents", Set.of("bucket", "s3Key", "sha256"))
    );

    private static final Map<String, Map<String, String>> FIELD_LABELS = Map.ofEntries(
            entry("projects", Map.ofEntries(
                    entry("name", "Nome do projeto"),
                    entry("code", "Codigo do projeto"),
                    entry("object", "Objeto do projeto"),
                    entry("primaryPartner", "Parceiro primario"),
                    entry("secundaryPartner", "Parceiro secundario"),
                    entry("primaryClient", "Cliente primario"),
                    entry("secundaryClient", "Cliente secundario"),
                    entry("cordinator", "Coordenador"),
                    entry("projectGovIf", "Unidade GOV/IF"),
                    entry("projectType", "Tipo do projeto"),
                    entry("projectStatus", "Status do projeto"),
                    entry("contractValue", "Valor do contrato"),
                    entry("startDate", "Data de inicio"),
                    entry("endDate", "Data de termino"),
                    entry("openingDate", "Data de abertura"),
                    entry("closingDate", "Data de encerramento"),
                    entry("city", "Cidade"),
                    entry("state", "Estado"),
                    entry("executionLocation", "Local de execucao"),
                    entry("areaSegmento", "Area/segmento")
            )),
            entry("documents", Map.ofEntries(
                    entry("ownerType", "Tipo do dono"),
                    entry("ownerId", "Dono"),
                    entry("category", "Categoria"),
                    entry("originalName", "Nome do arquivo"),
                    entry("contentType", "Tipo do arquivo"),
                    entry("sizeBytes", "Tamanho do arquivo"),
                    entry("status", "Status do arquivo")
            )),
            entry("budget-categories", Map.ofEntries(
                    entry("code", "Codigo da rubrica"),
                    entry("name", "Nome da rubrica"),
                    entry("description", "Descricao da rubrica")
            )),
            entry("budget-items", Map.ofEntries(
                    entry("category", "Rubrica"),
                    entry("description", "Descricao do item"),
                    entry("quantity", "Quantidade"),
                    entry("months", "Meses"),
                    entry("unitCost", "Custo unitario"),
                    entry("plannedAmount", "Valor planejado"),
                    entry("executedAmount", "Valor executado"),
                    entry("goal", "Meta"),
                    entry("notes", "Observacoes")
            )),
            entry("budget-transfers", Map.ofEntries(
                    entry("fromItem", "Item de origem"),
                    entry("toItem", "Item de destino"),
                    entry("amount", "Valor remanejado"),
                    entry("transferDate", "Data do remanejamento"),
                    entry("status", "Status do remanejamento"),
                    entry("reason", "Motivo"),
                    entry("document", "Documento")
            )),
            entry("disbursement-schedules", Map.ofEntries(
                    entry("numero", "Numero"),
                    entry("expectedMonth", "Mes previsto"),
                    entry("expectedAmount", "Valor previsto"),
                    entry("status", "Status do desembolso"),
                    entry("notes", "Observacoes")
            )),
            entry("goals", Map.ofEntries(
                    entry("numero", "Numero da meta"),
                    entry("titulo", "Titulo da meta"),
                    entry("descricao", "Descricao da meta"),
                    entry("dataInicio", "Data de inicio"),
                    entry("dataFim", "Data de termino"),
                    entry("dataConclusao", "Data de conclusao")
            )),
            entry("stages", Map.ofEntries(
                    entry("goal", "Meta"),
                    entry("numero", "Numero da etapa"),
                    entry("titulo", "Titulo da etapa"),
                    entry("descricao", "Descricao da etapa"),
                    entry("dataInicio", "Data de inicio"),
                    entry("dataFim", "Data de termino"),
                    entry("dataConclusao", "Data de conclusao")
            )),
            entry("phases", Map.ofEntries(
                    entry("stage", "Etapa"),
                    entry("numero", "Numero da fase"),
                    entry("titulo", "Titulo da fase"),
                    entry("descricao", "Descricao da fase"),
                    entry("dataInicio", "Data de inicio"),
                    entry("dataFim", "Data de termino"),
                    entry("dataConclusao", "Data de conclusao")
            )),
            entry("incomes", Map.ofEntries(
                    entry("numero", "Numero da receita"),
                    entry("amount", "Valor da receita"),
                    entry("receivedAt", "Data do recebimento"),
                    entry("source", "Fonte"),
                    entry("invoiceNumber", "Numero da nota"),
                    entry("notes", "Observacoes")
            )),
            entry("expenses", Map.ofEntries(
                    entry("budgetItem", "Item de rubrica"),
                    entry("category", "Rubrica"),
                    entry("income", "Receita"),
                    entry("expenseDate", "Data da despesa"),
                    entry("quantity", "Quantidade"),
                    entry("amount", "Valor da despesa"),
                    entry("person", "Pessoa"),
                    entry("organization", "Organizacao"),
                    entry("description", "Descricao da despesa"),
                    entry("invoiceNumber", "Numero da nota"),
                    entry("invoiceDate", "Data da nota"),
                    entry("document", "Documento")
            )),
            entry("project-people", Map.ofEntries(
                    entry("person", "Pessoa"),
                    entry("role", "Papel"),
                    entry("workloadHours", "Carga horaria"),
                    entry("institutionalLink", "Vinculo institucional"),
                    entry("contractType", "Tipo de contrato"),
                    entry("startDate", "Data de inicio"),
                    entry("endDate", "Data de termino"),
                    entry("status", "Status"),
                    entry("baseAmount", "Valor base"),
                    entry("notes", "Observacoes")
            )),
            entry("project-companies", Map.ofEntries(
                    entry("company", "Empresa"),
                    entry("contractNumber", "Numero do contrato"),
                    entry("description", "Descricao"),
                    entry("startDate", "Data de inicio"),
                    entry("endDate", "Data de termino"),
                    entry("status", "Status"),
                    entry("totalValue", "Valor total"),
                    entry("notes", "Observacoes"),
                    entry("isIncubated", "Incubada"),
                    entry("serviceType", "Tipo de servico")
            )),
            entry("project-organizations", Map.ofEntries(
                    entry("company", "Organizacao"),
                    entry("contractNumber", "Numero do contrato"),
                    entry("description", "Descricao"),
                    entry("startDate", "Data de inicio"),
                    entry("endDate", "Data de termino"),
                    entry("status", "Status"),
                    entry("totalValue", "Valor total"),
                    entry("notes", "Observacoes"),
                    entry("isIncubated", "Incubada"),
                    entry("serviceType", "Tipo de servico")
            )),
            entry("project_organization", Map.ofEntries(
                    entry("company", "Organizacao"),
                    entry("contractNumber", "Numero do contrato"),
                    entry("description", "Descricao"),
                    entry("startDate", "Data de inicio"),
                    entry("endDate", "Data de termino"),
                    entry("status", "Status"),
                    entry("totalValue", "Valor total"),
                    entry("notes", "Observacoes"),
                    entry("isIncubated", "Incubada"),
                    entry("serviceType", "Tipo de servico")
            ))
    );

    public ContractAuditDelta resolve(
            String resourceKey,
            String actionCode,
            Map<String, Object> beforeSnapshot,
            Map<String, Object> afterSnapshot
    ) {
        String normalizedResource = normalizeResourceKey(resourceKey);
        if (normalizedResource == null) {
            return ContractAuditDelta.conservative();
        }

        boolean reliable = switch (normalizeAction(actionCode)) {
            case "CRIAR" -> isUsableSnapshot(afterSnapshot);
            case "EXCLUIR" -> isUsableSnapshot(beforeSnapshot);
            default -> isUsableSnapshot(beforeSnapshot) && isUsableSnapshot(afterSnapshot);
        };

        if (!reliable) {
            return ContractAuditDelta.conservative();
        }

        List<AuditFieldChange> changes = buildChanges(
                normalizedResource,
                beforeSnapshot == null ? Map.of() : beforeSnapshot,
                afterSnapshot == null ? Map.of() : afterSnapshot
        );
        return ContractAuditDelta.reliable(changes);
    }

    private List<AuditFieldChange> buildChanges(
            String resourceKey,
            Map<String, Object> beforeSnapshot,
            Map<String, Object> afterSnapshot
    ) {
        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(beforeSnapshot.keySet());
        keys.addAll(afterSnapshot.keySet());

        List<AuditFieldChange> changes = new ArrayList<>();
        for (String key : keys) {
            if (shouldIgnoreField(resourceKey, key)) {
                continue;
            }

            Object beforeValue = normalizeSnapshotValue(beforeSnapshot.get(key));
            Object afterValue = normalizeSnapshotValue(afterSnapshot.get(key));
            if (valuesEquivalent(beforeValue, afterValue)) {
                continue;
            }

            String label = resolveFieldLabel(resourceKey, key);
            Object oldValue = formatValue(beforeValue);
            Object newValue = formatValue(afterValue);
            String type = oldValue == null ? "ADICIONADO" : newValue == null ? "REMOVIDO" : "EDITADO";
            changes.add(new AuditFieldChange(label, oldValue, newValue, type));
        }

        return changes;
    }

    private boolean shouldIgnoreField(String resourceKey, String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return true;
        }
        if (GENERIC_IGNORED_FIELDS.contains(fieldName)) {
            return true;
        }
        Set<String> resourceIgnored = RESOURCE_IGNORED_FIELDS.get(resourceKey);
        return resourceIgnored != null && resourceIgnored.contains(fieldName);
    }

    private String resolveFieldLabel(String resourceKey, String fieldName) {
        Map<String, String> labels = FIELD_LABELS.get(resourceKey);
        if (labels != null && labels.containsKey(fieldName)) {
            return labels.get(fieldName);
        }
        return humanize(fieldName);
    }

    private boolean isUsableSnapshot(Map<String, Object> snapshot) {
        return snapshot != null && !snapshot.isEmpty() && !looksLikeTechnicalPayload(snapshot);
    }

    private boolean looksLikeTechnicalPayload(Map<String, Object> snapshot) {
        return snapshot.containsKey("resource")
                && snapshot.containsKey("path")
                && snapshot.containsKey("method")
                && snapshot.containsKey("actionLabel");
    }

    private Object normalizeSnapshotValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            String normalizedTemporal = normalizeTemporalString(trimmed);
            return normalizedTemporal != null ? normalizedTemporal : trimmed;
        }
        if (value instanceof BigDecimal decimal) {
            return normalizeDecimal(decimal);
        }
        if (value instanceof Number number) {
            return normalizeDecimal(new BigDecimal(number.toString()));
        }
        if (value instanceof Boolean) {
            return value;
        }
        if (value instanceof Enum<?> enumeration) {
            return enumeration.name();
        }
        if (value instanceof UUID uuid) {
            return uuid.toString();
        }
        if (value instanceof LocalDate date) {
            return date.toString();
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toString();
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toString();
        }
        if (value instanceof List<?> list) {
            List<Object> normalized = new ArrayList<>(list.size());
            for (Object item : list) {
                normalized.add(normalizeSnapshotValue(item));
            }
            return normalized;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                normalized.put(String.valueOf(entry.getKey()), normalizeSnapshotValue(entry.getValue()));
            }
            return normalized;
        }
        return String.valueOf(value).trim();
    }

    private boolean valuesEquivalent(Object beforeValue, Object afterValue) {
        if (beforeValue == null && afterValue == null) {
            return true;
        }
        if (beforeValue == null || afterValue == null) {
            return false;
        }
        if (beforeValue instanceof BigDecimal leftDecimal && afterValue instanceof BigDecimal rightDecimal) {
            return leftDecimal.compareTo(rightDecimal) == 0;
        }
        return Objects.equals(beforeValue, afterValue);
    }

    private Object formatValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (value instanceof List<?> list) {
            return "lista(" + list.size() + ")";
        }
        if (value instanceof Map<?, ?> map) {
            return "mapa(" + map.size() + ")";
        }
        return value;
    }

    private BigDecimal normalizeDecimal(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.stripTrailingZeros();
    }

    private String normalizeTemporalString(String value) {
        try {
            return LocalDate.parse(value).toString();
        } catch (DateTimeParseException ignored) {
            // ignore
        }
        try {
            return LocalDateTime.parse(value).toString();
        } catch (DateTimeParseException ignored) {
            // ignore
        }
        try {
            return OffsetDateTime.parse(value).toString();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String humanize(String fieldName) {
        String value = fieldName
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replace('_', ' ')
                .trim();
        if (value.isEmpty()) {
            return "Campo";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase(Locale.ROOT);
    }

    private String normalizeAction(String actionCode) {
        if (actionCode == null || actionCode.isBlank()) {
            return "ATUALIZAR";
        }
        return actionCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeResourceKey(String resourceKey) {
        if (resourceKey == null || resourceKey.isBlank()) {
            return null;
        }
        int dotIndex = resourceKey.indexOf('.');
        if (dotIndex < 0) {
            return resourceKey.trim();
        }
        return resourceKey.substring(0, dotIndex).trim();
    }

    public record ContractAuditDelta(boolean reliable, String source, List<AuditFieldChange> changes) {

        public static ContractAuditDelta reliable(List<AuditFieldChange> changes) {
            return new ContractAuditDelta(true, "SNAPSHOT_DIFF", changes == null ? List.of() : changes);
        }

        public static ContractAuditDelta conservative() {
            return new ContractAuditDelta(false, "CONSERVATIVE", List.of());
        }
    }
}
