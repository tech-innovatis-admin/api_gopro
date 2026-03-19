package br.com.gopro.api.service.audit;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ContractAuditDeltaResolverTest {

    @Test
    void resolve_projectsSingleKnownField_shouldReturnFriendlyRealDelta() {
        ContractAuditDeltaResolver.ContractAuditDelta result = new ContractAuditDeltaResolver().resolve(
                "projects",
                "ATUALIZAR",
                Map.of(
                        "name", "Contrato A",
                        "contractValue", new BigDecimal("1000.00"),
                        "updatedAt", "2026-03-10T10:00:00"
                ),
                Map.of(
                        "name", "Contrato A",
                        "contractValue", new BigDecimal("1250.00"),
                        "updatedAt", "2026-03-10T10:05:00"
                )
        );

        assertThat(result.reliable()).isTrue();
        assertThat(result.changes()).containsExactly(
                new AuditFieldChange("Valor do contrato", "1000", "1250", "EDITADO")
        );
    }

    @Test
    void resolve_projectsMultipleKnownFields_shouldReturnOnlyEffectiveChanges() {
        ContractAuditDeltaResolver.ContractAuditDelta result = new ContractAuditDeltaResolver().resolve(
                "projects",
                "ATUALIZAR",
                Map.of(
                        "name", "Contrato A",
                        "contractValue", new BigDecimal("1000.00"),
                        "endDate", LocalDate.of(2026, 3, 31)
                ),
                Map.of(
                        "name", "Contrato B",
                        "contractValue", new BigDecimal("1500.00"),
                        "endDate", LocalDate.of(2026, 4, 30)
                )
        );

        assertThat(result.reliable()).isTrue();
        assertThat(result.changes()).containsExactlyInAnyOrder(
                new AuditFieldChange("Nome do projeto", "Contrato A", "Contrato B", "EDITADO"),
                new AuditFieldChange("Valor do contrato", "1000", "1500", "EDITADO"),
                new AuditFieldChange("Data de termino", "2026-03-31", "2026-04-30", "EDITADO")
        );
    }

    @Test
    void resolve_projectsEquivalentValues_shouldNotGenerateNoise() {
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("contractValue", new BigDecimal("1000.0"));
        before.put("city", null);

        ContractAuditDeltaResolver.ContractAuditDelta result = new ContractAuditDeltaResolver().resolve(
                "projects",
                "ATUALIZAR",
                before,
                Map.of(
                        "contractValue", new BigDecimal("1000.00"),
                        "city", "   "
                )
        );

        assertThat(result.reliable()).isTrue();
        assertThat(result.changes()).isEmpty();
    }

    @Test
    void resolve_documentsCreate_shouldUseRepositorySnapshotAsTrustedDelta() {
        ContractAuditDeltaResolver.ContractAuditDelta result = new ContractAuditDeltaResolver().resolve(
                "documents",
                "CRIAR",
                null,
                Map.of(
                        "originalName", "contrato.pdf",
                        "category", "Contrato",
                        "status", "AVAILABLE",
                        "sizeBytes", 2048L,
                        "bucket", "private-bucket"
                )
        );

        assertThat(result.reliable()).isTrue();
        assertThat(result.changes()).containsExactlyInAnyOrder(
                new AuditFieldChange("Categoria", null, "Contrato", "ADICIONADO"),
                new AuditFieldChange("Nome do arquivo", null, "contrato.pdf", "ADICIONADO"),
                new AuditFieldChange("Status do arquivo", null, "AVAILABLE", "ADICIONADO"),
                new AuditFieldChange("Tamanho do arquivo", null, "2048", "ADICIONADO")
        );
    }

    @Test
    void resolve_budgetCategoryUpdate_shouldReturnFriendlyRubricaDelta() {
        ContractAuditDeltaResolver.ContractAuditDelta result = new ContractAuditDeltaResolver().resolve(
                "budget-categories",
                "ATUALIZAR",
                Map.of("name", "Rubrica antiga", "description", "Descricao antiga"),
                Map.of("name", "Rubrica nova", "description", "Descricao antiga")
        );

        assertThat(result.reliable()).isTrue();
        assertThat(result.changes()).containsExactly(
                new AuditFieldChange("Nome da rubrica", "Rubrica antiga", "Rubrica nova", "EDITADO")
        );
    }

    @Test
    void resolve_goalUpdate_shouldReturnFriendlyMetaDelta() {
        ContractAuditDeltaResolver.ContractAuditDelta result = new ContractAuditDeltaResolver().resolve(
                "goals",
                "ATUALIZAR",
                Map.of("titulo", "Meta 1", "dataFim", LocalDate.of(2026, 5, 30)),
                Map.of("titulo", "Meta 2", "dataFim", LocalDate.of(2026, 6, 15))
        );

        assertThat(result.reliable()).isTrue();
        assertThat(result.changes()).containsExactlyInAnyOrder(
                new AuditFieldChange("Titulo da meta", "Meta 1", "Meta 2", "EDITADO"),
                new AuditFieldChange("Data de termino", "2026-05-30", "2026-06-15", "EDITADO")
        );
    }

    @Test
    void resolve_goalFinancialUpdate_shouldReturnFriendlyFinancialDelta() {
        Map<String, Object> before = new HashMap<>();
        before.put("hasFinancialValue", false);
        before.put("financialAmount", null);

        Map<String, Object> after = new HashMap<>();
        after.put("hasFinancialValue", true);
        after.put("financialAmount", new BigDecimal("15000.00"));

        ContractAuditDeltaResolver.ContractAuditDelta result = new ContractAuditDeltaResolver().resolve(
                "goals",
                "ATUALIZAR",
                before,
                after
        );

        assertThat(result.reliable()).isTrue();
        assertThat(result.changes()).containsExactlyInAnyOrder(
                new AuditFieldChange("Meta com valor financeiro", false, true, "EDITADO"),
                new AuditFieldChange("Valor financeiro da meta", null, "15000", "ADICIONADO")
        );
    }

    @Test
    void resolve_stageFinancialUpdate_shouldReturnFriendlyStageFinancialDelta() {
        Map<String, Object> before = new HashMap<>();
        before.put("hasFinancialValue", false);
        before.put("financialAmount", null);

        Map<String, Object> after = new HashMap<>();
        after.put("hasFinancialValue", true);
        after.put("financialAmount", new BigDecimal("3200.00"));

        ContractAuditDeltaResolver.ContractAuditDelta result = new ContractAuditDeltaResolver().resolve(
                "stages",
                "ATUALIZAR",
                before,
                after
        );

        assertThat(result.reliable()).isTrue();
        assertThat(result.changes()).containsExactlyInAnyOrder(
                new AuditFieldChange("Etapa com valor financeiro", false, true, "EDITADO"),
                new AuditFieldChange("Valor financeiro da etapa", null, "3200", "ADICIONADO")
        );
    }

    @Test
    void resolve_projectPeopleUpdate_shouldReturnFriendlyPeopleDelta() {
        ContractAuditDeltaResolver.ContractAuditDelta result = new ContractAuditDeltaResolver().resolve(
                "project-people",
                "ATUALIZAR",
                Map.of("person", 10L, "baseAmount", new BigDecimal("500.00")),
                Map.of("person", 12L, "baseAmount", new BigDecimal("650.00"))
        );

        assertThat(result.reliable()).isTrue();
        assertThat(result.changes()).containsExactlyInAnyOrder(
                new AuditFieldChange("Pessoa", "10", "12", "EDITADO"),
                new AuditFieldChange("Valor base", "500", "650", "EDITADO")
        );
    }

    @Test
    void resolve_expenseLinkUpdate_shouldUseFriendlyPaymentLinkLabels() {
        Map<String, Object> before = new HashMap<>();
        before.put("person", 10L);
        before.put("organization", null);

        Map<String, Object> after = new HashMap<>();
        after.put("person", null);
        after.put("organization", 22L);

        ContractAuditDeltaResolver.ContractAuditDelta result = new ContractAuditDeltaResolver().resolve(
                "expenses",
                "ATUALIZAR",
                before,
                after
        );

        assertThat(result.reliable()).isTrue();
        assertThat(result.changes()).containsExactlyInAnyOrder(
                new AuditFieldChange("Pessoa vinculada", "10", null, "REMOVIDO"),
                new AuditFieldChange("Empresa vinculada", null, "22", "ADICIONADO")
        );
    }

    @Test
    void resolve_missingSnapshotsForUpdate_shouldStayConservative() {
        ContractAuditDeltaResolver.ContractAuditDelta result = new ContractAuditDeltaResolver().resolve(
                "projects",
                "ATUALIZAR",
                null,
                Map.of("name", "Contrato A")
        );

        assertThat(result.reliable()).isFalse();
        assertThat(result.source()).isEqualTo("CONSERVATIVE");
        assertThat(result.changes()).isEmpty();
    }
}
