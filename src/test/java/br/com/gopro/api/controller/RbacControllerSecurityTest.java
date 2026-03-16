package br.com.gopro.api.controller;

import br.com.gopro.api.config.JwtService;
import br.com.gopro.api.dtos.AllowedRegistrationResponseDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.enums.AllowedRegistrationStatusEnum;
import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.service.AllowedRegistrationService;
import br.com.gopro.api.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AdminAuditController.class,
        AllowedRegistrationAdminController.class
})
@Import(RbacControllerSecurityTest.TestMethodSecurityConfig.class)
class RbacControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private AllowedRegistrationService allowedRegistrationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserRepository appUserRepository;

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void generalAdminAuditEndpoint_shouldNotExist() throws Exception {
        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_shouldAlsoReceiveNotFoundForRemovedGeneralAuditEndpoint() throws Exception {
        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_shouldAccessContractAuditEndpointWhenContractIdIsProvided() throws Exception {
        when(auditLogService.list(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponseDTO<>(List.of(), 0, 20, 0, 0, true, true));

        mockMvc.perform(get("/audit-log").param("contractId", "42"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_shouldNotAccessContractAuditEndpointWithoutContractId() throws Exception {
        mockMvc.perform(get("/audit-log"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    void analista_shouldReceiveNotFoundForRemovedGeneralAuditEndpoint() throws Exception {
        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    void analista_shouldNotAccessContractAuditEndpoint() throws Exception {
        mockMvc.perform(get("/audit-log").param("contractId", "42"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void superadmin_shouldAccessAllowedRegistrationsAdminEndpoint() throws Exception {
        AllowedRegistrationResponseDTO row = new AllowedRegistrationResponseDTO(
                1L,
                "a@b.com",
                UserRoleEnum.ANALISTA,
                AllowedRegistrationStatusEnum.PENDING,
                10L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                null,
                null
        );
        when(allowedRegistrationService.listInvites(any(), anyInt(), anyInt()))
                .thenReturn(new PageResponseDTO<>(List.of(row), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/admin/allowed-registrations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_shouldNotAccessSuperadminOnlyAllowedRegistrationsEndpoint() throws Exception {
        mockMvc.perform(get("/admin/allowed-registrations"))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.GET, "/**").authenticated()
                            .anyRequest().authenticated()
                    )
                    .httpBasic(httpBasic -> {});
            return http.build();
        }

        @Bean
        UserDetailsService userDetailsService() {
            PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            return new InMemoryUserDetailsManager(
                    User.withUsername("admin").password(encoder.encode("123")).roles("ADMIN").build(),
                    User.withUsername("superadmin").password(encoder.encode("123")).roles("SUPERADMIN").build(),
                    User.withUsername("analista").password(encoder.encode("123")).roles("ANALISTA").build()
            );
        }
    }
}
