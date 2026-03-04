package br.com.gopro.api.service;

import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.model.AuditLog;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.repository.AuditLogRepository;
import br.com.gopro.api.service.audit.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private HttpServletRequest request;

    private AuditLogServiceImpl service;

    @BeforeEach
    void setup() {
        ObjectMapper mapper = new ObjectMapper();
        service = new AuditLogServiceImpl(
                auditLogRepository,
                appUserRepository,
                mapper,
                new AuditSensitiveDataMasker(mapper),
                new AuditDeltaCalculator(),
                new AuditMessageFormatter()
        );
    }

    @Test
    void log_shouldPersistAuditEntryWithBusinessAndTechnicalFields() {
        AppUser actor = new AppUser();
        actor.setId(7L);
        actor.setEmail("admin@empresa.com");
        actor.setFullName("Admin");

        when(appUserRepository.findById(7L)).thenReturn(Optional.of(actor));
        when(request.getHeader(anyString())).thenReturn(null);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");
        when(request.getHeader("X-Request-Id")).thenReturn("req-123");
        when(request.getRequestURI()).thenReturn("/admin/users/7");
        when(request.getMethod()).thenReturn("PATCH");
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditEventRequest event = AuditEventRequest.builder()
                .actorUserId(7L)
                .tipoAuditoria(AuditScopeEnum.USERS)
                .modulo("Usuarios")
                .feature("Gestao de usuarios")
                .entidadePrincipal("Usuario")
                .entidadeId("7")
                .acao("ATUALIZAR")
                .resultado(AuditResultEnum.SUCESSO)
                .antes(Map.of("email", "admin@empresa.com", "passwordHash", "abc"))
                .depois(Map.of("email", "novo@empresa.com", "passwordHash", "xyz"))
                .build();

        service.log(event, request);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getActorUser()).isNotNull();
        assertThat(saved.getActorUser().getId()).isEqualTo(7L);
        assertThat(saved.getTipoAuditoria()).isEqualTo(AuditScopeEnum.USERS);
        assertThat(saved.getModulo()).isEqualTo("Usuarios");
        assertThat(saved.getCorrelacaoId()).isEqualTo("req-123");
        assertThat(saved.getEventAt()).isNotNull();
        assertThat(saved.getAuditId()).isNotBlank();
        assertThat(saved.getBeforeJson()).contains("***");
        assertThat(saved.getAfterJson()).contains("***");
        assertThat(saved.getAlteracoesJson()).contains("EDITADO");
        assertThat(saved.getIp()).isEqualTo("10.0.0.1");
        assertThat(saved.getUserAgent()).isEqualTo("JUnit");
    }

    @Test
    void log_updateWithoutChanges_shouldGenerateBeforeAfterAndDelta() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getRequestURI()).thenReturn("/projects/10");
        when(request.getMethod()).thenReturn("PATCH");

        service.log(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.CONTRACTS)
                        .entidadePrincipal("Contrato")
                        .entidadeId("10")
                        .modulo("Contratos")
                        .feature("Edicao de contrato")
                        .acao("ATUALIZAR")
                        .antes(Map.of("contractValue", 1000))
                        .depois(Map.of("contractValue", 1200))
                        .build(),
                request
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getBeforeJson()).contains("1000");
        assertThat(saved.getAfterJson()).contains("1200");
        assertThat(saved.getAlteracoesJson()).contains("contractValue");
        assertThat(saved.getResumo()).contains("Contrato");
        assertThat(saved.getDescricao()).doesNotContain("endpoint", "payload", "dto", "entity", "pk", "fk");
    }

    @Test
    void masker_shouldNotLeakSensitiveValues() {
        AuditSensitiveDataMasker masker = new AuditSensitiveDataMasker(new ObjectMapper());
        var node = masker.toMaskedNode(Map.of(
                "password", "123",
                "Authorization", "Bearer abc",
                "profile", Map.of("token", "secret")
        ));

        String json = node.toString();
        assertThat(json).doesNotContain("123");
        assertThat(json).doesNotContain("Bearer abc");
        assertThat(json).doesNotContain("secret");
        assertThat(json).contains("***");
    }

    @Test
    void formatter_shouldAvoidForbiddenTechnicalTerms() {
        AuditMessage message = new AuditMessageFormatter().format(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.CONTRACTS)
                        .entidadePrincipal("Contrato")
                        .entidadeId("123")
                        .modulo("Contratos")
                        .feature("Edicao de contrato")
                        .aba("Financeiro")
                        .acao("ATUALIZAR")
                        .alteracoes(List.of(new AuditFieldChange("contractValue", 1000, 1200, "EDITADO")))
                        .build()
        );

        String combined = (message.resumo() + " " + message.descricao()).toLowerCase();
        assertThat(combined).doesNotContain("endpoint");
        assertThat(combined).doesNotContain("payload");
        assertThat(combined).doesNotContain("dto");
        assertThat(combined).doesNotContain("entity");
        assertThat(combined).doesNotContain("pk");
        assertThat(combined).doesNotContain("fk");
    }
}
