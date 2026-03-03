package br.com.gopro.api.service;

import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.model.AuditLog;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        service = new AuditLogServiceImpl(auditLogRepository, appUserRepository, new ObjectMapper());
    }

    @Test
    void log_shouldPersistAuditEntryWithJsonAndRequestMetadata() {
        AppUser actor = new AppUser();
        actor.setId(7L);
        actor.setEmail("admin@empresa.com");

        when(appUserRepository.findById(7L)).thenReturn(Optional.of(actor));
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.log(
                7L,
                AuditActions.INVITE_CREATED,
                "allowed_registrations",
                "15",
                Map.of("status", "PENDING"),
                Map.of("status", "PENDING", "email", "novo@empresa.com"),
                request
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getActorUser()).isNotNull();
        assertThat(saved.getActorUser().getId()).isEqualTo(7L);
        assertThat(saved.getAction()).isEqualTo(AuditActions.INVITE_CREATED);
        assertThat(saved.getEntityType()).isEqualTo("allowed_registrations");
        assertThat(saved.getEntityId()).isEqualTo("15");
        assertThat(saved.getBeforeJson()).contains("PENDING");
        assertThat(saved.getAfterJson()).contains("novo@empresa.com");
        assertThat(saved.getIp()).isEqualTo("10.0.0.1");
        assertThat(saved.getUserAgent()).isEqualTo("JUnit");
    }

    @Test
    void log_shouldSerializePlainStringAsValidJson() throws Exception {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.log(
                null,
                "ANY_ACTION",
                "users",
                "1",
                "plain-text",
                "{\"ok\":true}",
                null
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        ObjectMapper mapper = new ObjectMapper();
        assertThat(mapper.readTree(saved.getBeforeJson()).textValue()).isEqualTo("plain-text");
        assertThat(mapper.readTree(saved.getAfterJson()).get("ok").asBoolean()).isTrue();
    }
}
