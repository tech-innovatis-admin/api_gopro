package br.com.gopro.api.service.audit;

import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.service.AuditActions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuditMessageFormatterTest {

    private final AuditMessageFormatter formatter = new AuditMessageFormatter();

    @Test
    void format_shouldBuildAuthenticationNarrativeByFailureReason() {
        AuditMessage invalidCredentials = formatter.format(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.SYSTEM)
                        .feature("Login")
                        .acao("LOGIN")
                        .resultado(AuditResultEnum.FALHA)
                        .detalhesTecnicos(Map.of(
                                "auditAction", AuditActions.LOGIN_FAILED,
                                "reason", "NOT_FOUND"
                        ))
                        .build()
        );

        AuditMessage blocked = formatter.format(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.SYSTEM)
                        .feature("Login")
                        .acao("LOGIN")
                        .resultado(AuditResultEnum.FALHA)
                        .detalhesTecnicos(Map.of(
                                "auditAction", AuditActions.LOGIN_FAILED,
                                "reason", "INACTIVE_OR_DISABLED"
                        ))
                        .build()
        );

        assertThat(invalidCredentials.resumo()).isEqualTo("Tentativa de login negada");
        assertThat(invalidCredentials.descricao()).contains("credenciais inválidas");
        assertThat(blocked.resumo()).isEqualTo("Tentativa de login bloqueada");
        assertThat(blocked.descricao()).contains("usuário inativo ou bloqueado");
    }

    @Test
    void format_shouldBuildInviteNarrativeFromTechnicalAction() {
        AuditMessage createdInvite = formatter.format(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.USERS)
                        .feature("Convites de cadastro")
                        .entidadePrincipal("Convite de cadastro")
                        .acao("CRIAR")
                        .depois(Map.of("email", "analista@empresa.com"))
                        .detalhesTecnicos(Map.of(
                                "auditAction", AuditActions.INVITE_CREATED,
                                "inviteAction", AuditActions.INVITE_CREATED
                        ))
                        .build()
        );

        AuditMessage completedRegistration = formatter.format(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.USERS)
                        .feature("Cadastro por convite")
                        .entidadePrincipal("Usuario")
                        .acao("CRIAR")
                        .depois(Map.of("fullName", "Novo Usuário"))
                        .detalhesTecnicos(Map.of(
                                "auditAction", AuditActions.REGISTER_COMPLETED,
                                "inviteAction", AuditActions.REGISTER_COMPLETED
                        ))
                        .build()
        );

        assertThat(createdInvite.resumo()).isEqualTo("Convite de cadastro criado");
        assertThat(createdInvite.descricao()).isEqualTo("Convite emitido para analista@empresa.com.");
        assertThat(completedRegistration.resumo()).isEqualTo("Cadastro por convite concluído");
        assertThat(completedRegistration.descricao()).contains("Novo Usuário");
    }

    @Test
    void format_shouldBuildConservativeContractNarrative() {
        AuditMessage message = formatter.format(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.CONTRACTS)
                        .entidadePrincipal("Contrato")
                        .entidadeId("42")
                        .aba("Pagamentos")
                        .acao("ATUALIZAR")
                        .alteracoes(List.of(new AuditFieldChange("contractValue", null, 2000, "EDITADO")))
                        .detalhesTecnicos(Map.of("resource", "expenses"))
                        .build()
        );

        assertThat(message.resumo()).isEqualTo("Despesa atualizada no contrato #42");
        assertThat(message.descricao()).isEqualTo("Registro atualizado na aba Pagamentos.");
    }

    @Test
    void format_shouldBuildComebackNarrativeForBudgetTransferAudit() {
        AuditMessage message = formatter.format(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.CONTRACTS)
                        .entidadePrincipal("Contrato")
                        .entidadeId("42")
                        .aba("Rubricas")
                        .acao("CRIAR")
                        .depois(Map.of(
                                "reason", "Comeback do remanejamento #44. Motivo original: cadastro incorreto"
                        ))
                        .detalhesTecnicos(Map.of("resource", "budget-transfers"))
                        .build()
        );

        assertThat(message.resumo()).isEqualTo("Comeback do remanejamento #44 registrado no contrato #42");
        assertThat(message.descricao()).isEqualTo("Foi registrado um comeback para desfazer o remanejamento #44 na aba Rubricas.");
    }

    @Test
    void format_shouldUsePreciseProjectNarrativeWhenReliableDeltaHasSingleKnownField() {
        AuditMessage message = formatter.format(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.CONTRACTS)
                        .entidadePrincipal("Contrato")
                        .entidadeId("15")
                        .aba("Contrato")
                        .acao("ATUALIZAR")
                        .alteracoes(List.of(new AuditFieldChange("Valor do contrato", "1000.00", "1250.00", "EDITADO")))
                        .detalhesTecnicos(Map.of(
                                "resource", "projects",
                                "deltaReliable", true,
                                "deltaSource", "SNAPSHOT_DIFF"
                        ))
                        .build()
        );

        assertThat(message.resumo()).isEqualTo("Valor do contrato alterado no contrato #15");
        assertThat(message.descricao()).isEqualTo("Valor do contrato foi atualizado.");
    }

    @Test
    void enrichSummaryWithContractCode_shouldReplaceContractIdMarkers() {
        assertThat(formatter.enrichSummaryWithContractCode("Contrato #15 atualizado", 15L, "CT-2024-015"))
                .isEqualTo("Contrato CT-2024-015 atualizado");
        assertThat(formatter.enrichSummaryWithContractCode("Rubrica criada no contrato #15", 15L, "CT-2024-015"))
                .isEqualTo("Rubrica criada no contrato CT-2024-015");
    }
}
