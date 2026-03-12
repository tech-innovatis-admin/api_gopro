package br.com.gopro.api.service;

import br.com.gopro.api.dtos.AuditLogResponseDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.model.AuditLog;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.repository.AuditLogRepository;
import br.com.gopro.api.repository.ProjectRepository;
import br.com.gopro.api.service.audit.AuditDeltaCalculator;
import br.com.gopro.api.service.audit.AuditEventRequest;
import br.com.gopro.api.service.audit.AuditFieldChange;
import br.com.gopro.api.service.audit.AuditMessageFormatter;
import br.com.gopro.api.service.audit.AuditSensitiveDataMasker;
import br.com.gopro.api.service.audit.ContractAuditChangeEnricher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ContractAuditChangeEnricher contractAuditChangeEnricher;
    @Mock
    private HttpServletRequest request;

    private AuditLogServiceImpl service;

    @BeforeEach
    void setup() {
        ObjectMapper mapper = new ObjectMapper();
        service = new AuditLogServiceImpl(
                auditLogRepository,
                appUserRepository,
                projectRepository,
                mapper,
                new AuditSensitiveDataMasker(mapper),
                new AuditDeltaCalculator(),
                new AuditMessageFormatter(),
                contractAuditChangeEnricher
        );
    }

    @Test
    void log_shouldPersistCentralizedNarrativeForAdministrativeUserUpdate() {
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
                .modulo("Usuários")
                .feature("Gestão de usuários")
                .entidadePrincipal("Usuario")
                .entidadeId("7")
                .acao("ATUALIZAR")
                .resultado(AuditResultEnum.SUCESSO)
                .antes(Map.of("fullName", "Admin", "role", "ANALISTA"))
                .depois(Map.of("fullName", "Admin", "role", "ADMIN"))
                .alteracoes(List.of(new AuditFieldChange("role", "ANALISTA", "ADMIN", "EDITADO")))
                .detalhesTecnicos(Map.of("auditAction", AuditActions.USER_UPDATED))
                .build();

        service.log(event, request);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getResumo()).isEqualTo("Usuário Admin: perfil alterado");
        assertThat(saved.getDescricao()).isEqualTo("Perfil de acesso do usuário atualizado.");
        assertThat(saved.getAlteracoesJson()).contains("EDITADO");
        assertThat(saved.getIp()).isEqualTo("10.0.0.1");
        assertThat(saved.getUserAgent()).isEqualTo("JUnit");
    }

    @Test
    void log_contractEvent_shouldProduceConservativeNarrativeWithoutUiNoise() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getRequestURI()).thenReturn("/budget-categories/10");
        when(request.getMethod()).thenReturn("PATCH");

        service.log(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.CONTRACTS)
                        .entidadePrincipal("Contrato")
                        .entidadeId("10")
                        .modulo("Contratos")
                        .feature("Edicao de rubrica")
                        .aba("Rubricas")
                        .acao("ATUALIZAR")
                        .antes(Map.of("name", "Rubrica A"))
                        .depois(Map.of("name", "Rubrica B"))
                        .alteracoes(List.of(new AuditFieldChange("Nome", null, "Rubrica B", "EDITADO")))
                        .detalhesTecnicos(Map.of("resource", "budget-categories"))
                        .build(),
                request
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getResumo()).isEqualTo("Rubrica atualizada no contrato #10");
        assertThat(saved.getDescricao()).isEqualTo("Registro atualizado na aba Rubricas.");
        assertThat(saved.getDescricao()).doesNotContain("Tela", "Campos alterados");
    }

    @Test
    void log_contractProjectEvent_shouldUsePreciseNarrativeWhenReliableDeltaExists() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getRequestURI()).thenReturn("/projects/15");
        when(request.getMethod()).thenReturn("PATCH");

        service.log(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.CONTRACTS)
                        .entidadePrincipal("Contrato")
                        .entidadeId("15")
                        .modulo("Contratos")
                        .feature("Edicao de projeto")
                        .aba("Contrato")
                        .acao("ATUALIZAR")
                        .antes(Map.of("contractValue", "1000.00"))
                        .depois(Map.of("contractValue", "1250.00"))
                        .alteracoes(List.of(new AuditFieldChange("Valor do contrato", "1000.00", "1250.00", "EDITADO")))
                        .detalhesTecnicos(Map.of(
                                "resource", "projects",
                                "deltaReliable", true,
                                "deltaSource", "SNAPSHOT_DIFF",
                                "skipAutomaticDelta", true
                        ))
                        .build(),
                request
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getResumo()).isEqualTo("Valor do contrato alterado no contrato #15");
        assertThat(saved.getDescricao()).isEqualTo("Valor do contrato foi atualizado.");
        assertThat(saved.getAlteracoesJson()).contains("Valor do contrato", "1250.00");
    }

    @Test
    void log_contractEvent_shouldSkipAutomaticDeltaWhenResolverMarksFallbackConservative() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getRequestURI()).thenReturn("/projects/30");
        when(request.getMethod()).thenReturn("PATCH");

        service.log(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.CONTRACTS)
                        .entidadePrincipal("Contrato")
                        .entidadeId("30")
                        .modulo("Contratos")
                        .feature("Edicao de projeto")
                        .aba("Contrato")
                        .acao("ATUALIZAR")
                        .antes(Map.of("name", "Contrato A"))
                        .depois(Map.of("name", "Contrato B"))
                        .alteracoes(List.of())
                        .detalhesTecnicos(Map.of(
                                "resource", "projects",
                                "deltaReliable", false,
                                "deltaSource", "CONSERVATIVE",
                                "skipAutomaticDelta", true
                        ))
                        .build(),
                request
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getResumo()).isEqualTo("Contrato #30 atualizado");
        assertThat(saved.getDescricao()).isEqualTo("Dados do contrato foram atualizados.");
        assertThat(saved.getAlteracoesJson()).isEqualTo("[]");
    }

    @Test
    void log_contractEvent_shouldNotPersistArtificialSnapshotsWhenManualDeltaIsEnabled() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(request.getRequestURI()).thenReturn("/budget-categories/9");
        when(request.getMethod()).thenReturn("PATCH");

        service.log(
                AuditEventRequest.builder()
                        .tipoAuditoria(AuditScopeEnum.CONTRACTS)
                        .entidadePrincipal("Contrato")
                        .entidadeId("9")
                        .modulo("Contratos")
                        .feature("Edicao de rubrica")
                        .aba("Rubricas")
                        .acao("ATUALIZAR")
                        .antes(null)
                        .depois(Map.of(
                                "resource", "budget-categories",
                                "path", "/budget-categories/9",
                                "method", "PATCH",
                                "actionLabel", "Rubrica atualizada"
                        ))
                        .alteracoes(List.of())
                        .detalhesTecnicos(Map.of(
                                "resource", "budget-categories",
                                "deltaReliable", false,
                                "deltaSource", "CONSERVATIVE",
                                "skipAutomaticDelta", true
                        ))
                        .build(),
                request
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getBeforeJson()).isNull();
        assertThat(saved.getAfterJson()).isNull();
        assertThat(saved.getAlteracoesJson()).isEqualTo("[]");
    }

    @Test
    void list_shouldEnrichContractSummaryWithContractCodeWhenAvailable() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setAuditId("audit-1");
        log.setTipoAuditoria(AuditScopeEnum.CONTRACTS);
        log.setEntityType("contracts:rubrica");
        log.setEntityId("15");
        log.setResumo("Rubrica criada no contrato #15");
        log.setDescricao("Registro criado na aba Rubricas.");
        log.setEventAt(OffsetDateTime.now());
        log.setDetalhesTecnicosJson("{\"contractId\":15}");

        Project project = new Project();
        project.setId(15L);
        project.setCode("CT-2024-015");
        project.setName("Contrato Piloto");

        when(auditLogRepository.findAll(org.mockito.ArgumentMatchers.<Specification<AuditLog>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log)));
        when(projectRepository.findAllById(org.mockito.ArgumentMatchers.<Iterable<Long>>any()))
                .thenReturn(List.of(project));
        when(contractAuditChangeEnricher.enrich(any(), any())).thenReturn(Map.of());

        PageResponseDTO<AuditLogResponseDTO> response = service.list(
                null,
                null,
                AuditScopeEnum.CONTRACTS,
                null,
                null,
                null,
                15L,
                null,
                null,
                0,
                10
        );

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).resumo()).isEqualTo("Rubrica criada no contrato CT-2024-015");
    }

    @Test
    void list_shouldPreferEnrichedChangesJsonWhenAvailable() {
        AuditLog log = new AuditLog();
        log.setId(2L);
        log.setAuditId("audit-2");
        log.setTipoAuditoria(AuditScopeEnum.CONTRACTS);
        log.setEntityType("contracts:project-people");
        log.setEntityId("15");
        log.setResumo("Pessoa vinculada atualizada no contrato #15");
        log.setDescricao("Registro atualizado na aba Pessoas.");
        log.setAlteracoesJson("[{\"caminho\":\"Pessoa\",\"de\":\"44\",\"para\":\"52\",\"tipo\":\"EDITADO\"}]");
        log.setDetalhesTecnicosJson("{\"contractId\":15,\"resource\":\"project-people\"}");
        log.setEventAt(OffsetDateTime.now());

        when(auditLogRepository.findAll(org.mockito.ArgumentMatchers.<Specification<AuditLog>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log)));
        when(projectRepository.findAllById(org.mockito.ArgumentMatchers.<Iterable<Long>>any()))
                .thenReturn(List.of());
        when(contractAuditChangeEnricher.enrich(any(), any()))
                .thenReturn(Map.of(2L, "[{\"caminho\":\"Pessoa\",\"de\":\"44\",\"para\":\"52\",\"deLabel\":\"Maria Silva\",\"paraLabel\":\"João Souza\",\"tipo\":\"EDITADO\"}]"));

        PageResponseDTO<AuditLogResponseDTO> response = service.list(
                null,
                null,
                AuditScopeEnum.CONTRACTS,
                null,
                null,
                null,
                15L,
                null,
                null,
                0,
                10
        );

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).alteracoesJson()).contains("Maria Silva", "João Souza");
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
}
