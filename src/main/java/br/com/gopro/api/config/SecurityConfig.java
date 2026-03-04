package br.com.gopro.api.config;

import br.com.gopro.api.repository.*;
import br.com.gopro.api.service.AuditLogService;
import br.com.gopro.api.service.audit.AuditSnapshotExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Value("${app.cors.allowed-origins:}")
    private List<String> allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private List<String> allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private List<String> allowedHeaders;

    @Value("${app.cors.exposed-headers:Location}")
    private List<String> exposedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ApiMutationAuditFilter apiMutationAuditFilter
    ) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/health",
                                "/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers("/register/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/admin/allowed-registrations/**").hasRole("SUPERADMIN")
                        .requestMatchers("/admin/audit/**").hasRole("SUPERADMIN")
                        .requestMatchers("/audit-log/**").hasRole("SUPERADMIN")
                        .requestMatchers("/admin/users/**").hasAnyRole("SUPERADMIN", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(apiMutationAuditFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ApiMutationAuditFilter apiMutationAuditFilter(
            AuditLogService auditLogService,
            ObjectMapper objectMapper,
            AuditSnapshotExtractor auditSnapshotExtractor,
            ProjectRepository projectRepository,
            BudgetCategoryRepository budgetCategoryRepository,
            BudgetItemRepository budgetItemRepository,
            BudgetTransferRepository budgetTransferRepository,
            DisbursementScheduleRepository disbursementScheduleRepository,
            GoalRepository goalRepository,
            StageRepository stageRepository,
            PhaseRepository phaseRepository,
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository,
            ProjectPeopleRepository projectPeopleRepository,
            ProjectCompanyRepository projectCompanyRepository,
            DocumentRepository documentRepository
    ) {
        return new ApiMutationAuditFilter(
                auditLogService,
                objectMapper,
                auditSnapshotExtractor,
                projectRepository,
                budgetCategoryRepository,
                budgetItemRepository,
                budgetTransferRepository,
                disbursementScheduleRepository,
                goalRepository,
                stageRepository,
                phaseRepository,
                incomeRepository,
                expenseRepository,
                projectPeopleRepository,
                projectCompanyRepository,
                documentRepository
        );
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(sanitizeList(allowedOrigins));
        configuration.setAllowedMethods(sanitizeList(allowedMethods));
        configuration.setAllowedHeaders(sanitizeList(allowedHeaders));
        configuration.setExposedHeaders(sanitizeList(exposedHeaders));
        configuration.setAllowCredentials(allowCredentials);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> sanitizeList(List<String> values) {
        List<String> sanitized = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                sanitized.add(value.trim());
            }
        }
        return sanitized;
    }
}
