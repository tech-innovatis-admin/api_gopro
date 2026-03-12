package br.com.gopro.api.service.audit;

import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.service.AuditActions;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class AuditMessageFormatter {

    private static final String CONTRACT_ROOT_RESOURCE = "projects";
    private static final Set<String> PRECISE_PROJECT_FIELDS = Set.of(
            "Nome do projeto",
            "Codigo do projeto",
            "Objeto do projeto",
            "Parceiro primario",
            "Parceiro secundario",
            "Cliente primario",
            "Cliente secundario",
            "Coordenador",
            "Unidade GOV/IF",
            "Tipo do projeto",
            "Status do projeto",
            "Valor do contrato",
            "Data de inicio",
            "Data de termino",
            "Data de abertura",
            "Data de encerramento",
            "Cidade",
            "Estado",
            "Local de execucao",
            "Area/segmento"
    );

    public AuditMessage format(AuditEventRequest request) {
        if (request == null) {
            return genericMessage();
        }

        Map<String, Object> technical = asMap(request.getDetalhesTecnicos());
        String auditAction = resolveAuditAction(technical);

        if (isBootstrapAudit(request, auditAction)) {
            return formatBootstrap();
        }
        if (isInviteAudit(request, technical, auditAction)) {
            return formatInvites(request, technical, auditAction);
        }
        if (isAuthenticationAudit(request, technical, auditAction)) {
            return formatAuthentication(request, technical);
        }
        if (request.getTipoAuditoria() == AuditScopeEnum.CONTRACTS) {
            return formatContracts(request, technical);
        }
        if (request.getTipoAuditoria() == AuditScopeEnum.USERS) {
            return formatUsers(request, auditAction);
        }
        return genericMessage();
    }

    public String enrichSummaryWithContractCode(String summary, Long contractId, String contractCode) {
        if (summary == null || summary.isBlank() || contractId == null || contractCode == null || contractCode.isBlank()) {
            return summary;
        }

        String enriched = summary;
        enriched = enriched.replace("Contrato #" + contractId, "Contrato " + contractCode);
        enriched = enriched.replace("contrato #" + contractId, "contrato " + contractCode);
        enriched = enriched.replace("Contrato " + contractId, "Contrato " + contractCode);
        enriched = enriched.replace("contrato " + contractId, "contrato " + contractCode);
        return enriched;
    }

    private AuditMessage formatContracts(AuditEventRequest request, Map<String, Object> technical) {
        String action = normalizeAction(request.getAcao());
        String resource = resolveContractResource(request, technical);
        String contractTitle = formatContractTitle(request.getEntidadeId());
        String contractReference = formatContractReference(request.getEntidadeId());
        String aba = resolveContractAba(request.getAba(), resource);
        String fileName = extractFileName(technical);

        String summary;
        String description;
        if (CONTRACT_ROOT_RESOURCE.equals(resource)) {
            String preciseProjectField = resolvePreciseProjectField(request, technical);
            if (preciseProjectField != null) {
                String displayField = presentProjectFieldLabel(preciseProjectField);
                summary = displayField + " alterado no " + contractReference;
                description = displayField + " foi atualizado.";
                return new AuditMessage(limit(summary, 500), limit(description, 2000));
            }
            summary = switch (action) {
                case "CRIAR" -> contractTitle + " criado";
                case "EXCLUIR" -> contractTitle + " removido";
                default -> contractTitle + " atualizado";
            };
            description = switch (action) {
                case "CRIAR" -> "Dados iniciais do contrato foram registrados.";
                case "EXCLUIR" -> "Cadastro do contrato foi removido.";
                default -> "Dados do contrato foram atualizados.";
            };
            return new AuditMessage(limit(summary, 500), limit(description, 2000));
        }

        if ("documents".equals(resource)) {
            summary = switch (action) {
                case "CRIAR" -> "Arquivo adicionado ao " + contractReference;
                case "EXCLUIR" -> "Arquivo removido do " + contractReference;
                default -> "Arquivo atualizado no " + contractReference;
            };
            description = switch (action) {
                case "CRIAR" -> fileName == null
                        ? "Um arquivo do contrato foi adicionado."
                        : "O arquivo \"" + fileName + "\" foi adicionado.";
                case "EXCLUIR" -> fileName == null
                        ? "Um arquivo do contrato foi removido."
                        : "O arquivo \"" + fileName + "\" foi removido.";
                default -> fileName == null
                        ? "Um arquivo do contrato foi atualizado."
                        : "O arquivo \"" + fileName + "\" foi atualizado.";
            };
            return new AuditMessage(limit(summary, 500), limit(description, 2000));
        }

        summary = buildContractResourceSummary(resource, action, contractReference);
        description = switch (action) {
            case "CRIAR" -> "Registro criado na aba " + aba + ".";
            case "EXCLUIR" -> "Registro removido na aba " + aba + ".";
            default -> "Registro atualizado na aba " + aba + ".";
        };
        return new AuditMessage(limit(summary, 500), limit(description, 2000));
    }

    private AuditMessage formatAuthentication(AuditEventRequest request, Map<String, Object> technical) {
        String action = normalizeAction(request.getAcao());
        String reason = upper(trimToNull(extractString(technical, "reason")));

        if ("LOGOUT".equals(action)) {
            return new AuditMessage("Logout realizado", "Sessão encerrada.");
        }
        if (request.getResultado() == AuditResultEnum.SUCESSO) {
            return new AuditMessage("Login realizado com sucesso", "Usuário autenticado com sucesso.");
        }
        if ("INACTIVE_OR_DISABLED".equals(reason)) {
            return new AuditMessage(
                    "Tentativa de login bloqueada",
                    "Tentativa de autenticação negada. Motivo: usuário inativo ou bloqueado."
            );
        }
        if ("PASSWORD_MISMATCH".equals(reason)) {
            return new AuditMessage(
                    "Tentativa de login negada",
                    "Tentativa de autenticação negada. Motivo: senha incorreta."
            );
        }
        return new AuditMessage(
                "Tentativa de login negada",
                "Tentativa de autenticação negada. Motivo: credenciais inválidas."
        );
    }

    private AuditMessage formatInvites(AuditEventRequest request, Map<String, Object> technical, String auditAction) {
        String email = resolveInviteEmail(request, technical);
        String userName = resolveUserName(request);
        String entity = safeLower(request.getEntidadePrincipal());

        if (AuditActions.INVITE_CREATED.equals(auditAction)) {
            return new AuditMessage("Convite de cadastro criado", email == null ? "Convite emitido." : "Convite emitido para " + email + ".");
        }
        if (AuditActions.INVITE_REISSUED.equals(auditAction)) {
            return new AuditMessage("Convite de cadastro reemitido", email == null ? "Convite reenviado." : "Convite reenviado para " + email + ".");
        }
        if (AuditActions.INVITE_CANCELLED.equals(auditAction)) {
            return new AuditMessage("Convite de cadastro cancelado", email == null ? "Convite cancelado." : "Convite cancelado para " + email + ".");
        }
        if (AuditActions.INVITE_VALIDATED.equals(auditAction)) {
            return new AuditMessage("Convite de cadastro validado", "Convite validado para prosseguir com o cadastro.");
        }
        if (AuditActions.REGISTER_COMPLETED.equals(auditAction) && entity.contains("convite")) {
            return new AuditMessage("Convite de cadastro utilizado", "Convite encerrado após criação do usuário.");
        }
        if (AuditActions.REGISTER_COMPLETED.equals(auditAction)) {
            String description = userName == null
                    ? "Usuário criado a partir de convite válido."
                    : "Usuário " + userName + " criado a partir de convite válido.";
            return new AuditMessage("Cadastro por convite concluído", description);
        }

        return genericMessage();
    }

    private AuditMessage formatUsers(AuditEventRequest request, String auditAction) {
        if (AuditActions.USER_UPDATED.equals(auditAction)) {
            return formatAdministrativeUserUpdate(request);
        }

        String userRef = resolveUserReference(request);
        String action = normalizeAction(request.getAcao());
        return switch (action) {
            case "CRIAR" -> new AuditMessage(userRef + ": cadastro criado", "Cadastro de usuário registrado.");
            case "EXCLUIR" -> new AuditMessage(userRef + ": cadastro removido", "Cadastro de usuário removido.");
            default -> new AuditMessage(userRef + ": dados atualizados", "Dados do usuário foram atualizados.");
        };
    }

    private AuditMessage formatAdministrativeUserUpdate(AuditEventRequest request) {
        String userName = resolveUserName(request);
        String userRef = userName == null ? resolveUserReference(request) : "Usuário " + userName;
        List<AuditFieldChange> changes = safeChanges(request.getAlteracoes());

        boolean statusChanged = containsAnyPath(changes, "status");
        boolean roleChanged = containsAnyPath(changes, "role");

        if (statusChanged && !roleChanged) {
            return new AuditMessage(userRef + ": status alterado", "Status do usuário atualizado na administração.");
        }
        if (roleChanged && !statusChanged) {
            return new AuditMessage(userRef + ": perfil alterado", "Perfil de acesso do usuário atualizado.");
        }
        return new AuditMessage(userRef + ": dados atualizados", "Dados administrativos do usuário atualizados.");
    }

    private AuditMessage formatBootstrap() {
        return new AuditMessage(
                "Superadmin provisionado",
                "Conta superadministradora preparada pelo bootstrap do ambiente."
        );
    }

    private AuditMessage genericMessage() {
        return new AuditMessage(
                "Evento de auditoria registrado",
                "Evento registrado sem narrativa específica de negócio."
        );
    }

    private boolean isBootstrapAudit(AuditEventRequest request, String auditAction) {
        return AuditActions.SUPERADMIN_BOOTSTRAPPED.equals(auditAction)
                || safeLower(request.getFeature()).contains("bootstrap");
    }

    private boolean isInviteAudit(AuditEventRequest request, Map<String, Object> technical, String auditAction) {
        return auditAction.startsWith("INVITE_")
                || AuditActions.REGISTER_COMPLETED.equals(auditAction)
                || safeLower(extractString(technical, "inviteAction")).contains("invite")
                || safeLower(request.getFeature()).contains("convite");
    }

    private boolean isAuthenticationAudit(AuditEventRequest request, Map<String, Object> technical, String auditAction) {
        String action = normalizeAction(request.getAcao());
        return "LOGIN".equals(action)
                || "LOGOUT".equals(action)
                || AuditActions.LOGIN_SUCCESS.equals(auditAction)
                || AuditActions.LOGIN_FAILED.equals(auditAction)
                || "login".equals(safeLower(request.getFeature()))
                || extractString(technical, "reason") != null;
    }

    private String resolveContractResource(AuditEventRequest request, Map<String, Object> technical) {
        String resource = normalizeResourceKey(extractString(technical, "resource"));
        if (resource != null) {
            return resource;
        }

        String subsecao = safeLower(request.getSubsecao());
        if (subsecao.contains("remanej")) {
            return "budget-transfers";
        }
        if (subsecao.contains("item") && subsecao.contains("rubrica")) {
            return "budget-items";
        }
        if (subsecao.contains("rubrica")) {
            return "budget-categories";
        }
        if (subsecao.contains("despesa")) {
            return "expenses";
        }
        if (subsecao.contains("receita")) {
            return "incomes";
        }
        if (subsecao.contains("fase")) {
            return "phases";
        }
        if (subsecao.contains("etapa")) {
            return "stages";
        }
        if (subsecao.contains("meta")) {
            return "goals";
        }

        String aba = safeLower(request.getAba());
        if (aba.contains("arquivo")) {
            return "documents";
        }
        if (aba.contains("pessoa")) {
            return "project-people";
        }
        if (aba.contains("empresa")) {
            return "project-companies";
        }
        if (aba.contains("meta")) {
            return "goals";
        }
        if (aba.contains("desembolso")) {
            return "disbursement-schedules";
        }
        if (aba.contains("rubrica")) {
            return "budget-categories";
        }
        if (aba.contains("pagamento")) {
            return "expenses";
        }

        String entidade = safeLower(request.getEntidadePrincipal());
        if (entidade.contains("contrato")) {
            return "projects";
        }
        return "unknown";
    }

    private String resolveContractAba(String rawAba, String resource) {
        String normalized = trimToNull(rawAba);
        if (normalized != null) {
            return normalized;
        }
        return switch (resource) {
            case "projects" -> "Contrato";
            case "documents" -> "Arquivos";
            case "budget-categories", "budget-items", "budget-transfers" -> "Rubricas";
            case "disbursement-schedules" -> "Desembolso";
            case "goals", "stages", "phases" -> "Metas";
            case "incomes", "expenses" -> "Pagamentos";
            case "project-people" -> "Pessoas";
            case "project-companies", "project-organizations", "project_organization" -> "Empresas";
            default -> "Contrato";
        };
    }

    private String buildContractResourceSummary(String resource, String action, String contractReference) {
        return switch (resource) {
            case "budget-categories" -> switch (action) {
                case "CRIAR" -> "Rubrica criada no " + contractReference;
                case "EXCLUIR" -> "Rubrica removida do " + contractReference;
                default -> "Rubrica atualizada no " + contractReference;
            };
            case "budget-items" -> switch (action) {
                case "CRIAR" -> "Item de rubrica criado no " + contractReference;
                case "EXCLUIR" -> "Item de rubrica removido do " + contractReference;
                default -> "Item de rubrica atualizado no " + contractReference;
            };
            case "budget-transfers" -> switch (action) {
                case "CRIAR" -> "Remanejamento registrado no " + contractReference;
                case "EXCLUIR" -> "Remanejamento removido do " + contractReference;
                default -> "Remanejamento atualizado no " + contractReference;
            };
            case "disbursement-schedules" -> switch (action) {
                case "CRIAR" -> "Desembolso criado no " + contractReference;
                case "EXCLUIR" -> "Desembolso removido do " + contractReference;
                default -> "Desembolso atualizado no " + contractReference;
            };
            case "goals" -> switch (action) {
                case "CRIAR" -> "Meta criada no " + contractReference;
                case "EXCLUIR" -> "Meta removida do " + contractReference;
                default -> "Meta atualizada no " + contractReference;
            };
            case "stages" -> switch (action) {
                case "CRIAR" -> "Etapa criada no " + contractReference;
                case "EXCLUIR" -> "Etapa removida do " + contractReference;
                default -> "Etapa atualizada no " + contractReference;
            };
            case "phases" -> switch (action) {
                case "CRIAR" -> "Fase criada no " + contractReference;
                case "EXCLUIR" -> "Fase removida do " + contractReference;
                default -> "Fase atualizada no " + contractReference;
            };
            case "incomes" -> switch (action) {
                case "CRIAR" -> "Receita registrada no " + contractReference;
                case "EXCLUIR" -> "Receita removida do " + contractReference;
                default -> "Receita atualizada no " + contractReference;
            };
            case "expenses" -> switch (action) {
                case "CRIAR" -> "Despesa registrada no " + contractReference;
                case "EXCLUIR" -> "Despesa removida do " + contractReference;
                default -> "Despesa atualizada no " + contractReference;
            };
            case "project-people" -> switch (action) {
                case "CRIAR" -> "Pessoa vinculada adicionada ao " + contractReference;
                case "EXCLUIR" -> "Pessoa vinculada removida do " + contractReference;
                default -> "Pessoa vinculada atualizada no " + contractReference;
            };
            case "project-companies" -> switch (action) {
                case "CRIAR" -> "Empresa vinculada adicionada ao " + contractReference;
                case "EXCLUIR" -> "Empresa vinculada removida do " + contractReference;
                default -> "Empresa vinculada atualizada no " + contractReference;
            };
            case "project-organizations", "project_organization" -> switch (action) {
                case "CRIAR" -> "Organização vinculada adicionada ao " + contractReference;
                case "EXCLUIR" -> "Organização vinculada removida do " + contractReference;
                default -> "Organização vinculada atualizada no " + contractReference;
            };
            default -> switch (action) {
                case "CRIAR" -> "Registro criado no " + contractReference;
                case "EXCLUIR" -> "Registro removido do " + contractReference;
                default -> "Registro atualizado no " + contractReference;
            };
        };
    }

    private String resolvePreciseProjectField(AuditEventRequest request, Map<String, Object> technical) {
        if (!"ATUALIZAR".equals(normalizeAction(request.getAcao()))) {
            return null;
        }
        if (!isReliableContractDelta(technical)) {
            return null;
        }

        List<AuditFieldChange> changes = safeChanges(request.getAlteracoes());
        if (changes.size() != 1) {
            return null;
        }

        String label = trimToNull(changes.get(0).caminho());
        if (label == null || !PRECISE_PROJECT_FIELDS.contains(label)) {
            return null;
        }
        return label;
    }

    private boolean isReliableContractDelta(Map<String, Object> technical) {
        Object candidate = technical.get("deltaReliable");
        if (candidate instanceof Boolean value) {
            return value;
        }
        if (candidate instanceof String text) {
            return Boolean.parseBoolean(text.trim());
        }
        return false;
    }

    private String formatContractTitle(String entityId) {
        String normalized = trimToNull(entityId);
        if (normalized == null) {
            return "Contrato";
        }
        return "Contrato #" + normalized;
    }

    private String formatContractReference(String entityId) {
        String normalized = trimToNull(entityId);
        if (normalized == null) {
            return "contrato";
        }
        return "contrato #" + normalized;
    }

    private String resolveAuditAction(Map<String, Object> technical) {
        String auditAction = trimToNull(extractString(technical, "auditAction"));
        if (auditAction != null) {
            return auditAction.toUpperCase(Locale.ROOT);
        }
        String inviteAction = trimToNull(extractString(technical, "inviteAction"));
        if (inviteAction != null) {
            return inviteAction.toUpperCase(Locale.ROOT);
        }
        return "";
    }

    private String resolveInviteEmail(AuditEventRequest request, Map<String, Object> technical) {
        String email = resolveEmail(request);
        if (email != null) {
            return email;
        }
        return trimToNull(extractString(technical, "email"));
    }

    private String resolveEmail(AuditEventRequest request) {
        String fromAfter = extractString(asMap(request.getDepois()), "email");
        if (fromAfter != null) {
            return fromAfter;
        }
        return extractString(asMap(request.getAntes()), "email");
    }

    private String resolveUserName(AuditEventRequest request) {
        String fromAfter = extractString(asMap(request.getDepois()), "fullName");
        if (fromAfter != null) {
            return fromAfter;
        }
        return extractString(asMap(request.getAntes()), "fullName");
    }

    private String resolveUserReference(AuditEventRequest request) {
        String userName = resolveUserName(request);
        if (userName != null) {
            return "Usuário " + userName;
        }
        String email = resolveEmail(request);
        if (email != null) {
            return "Usuário " + email;
        }
        if (request.getEntidadeId() != null && !request.getEntidadeId().isBlank()) {
            return "Usuário #" + request.getEntidadeId().trim();
        }
        return "Usuário";
    }

    private String presentProjectFieldLabel(String label) {
        if (label == null || label.isBlank()) {
            return "";
        }

        return label
                .replace("Codigo", "Código")
                .replace("primario", "primário")
                .replace("secundario", "secundário")
                .replace("inicio", "início")
                .replace("termino", "término")
                .replace("execucao", "execução")
                .replace("Area", "Área");
    }

    private boolean containsAnyPath(List<AuditFieldChange> changes, String token) {
        String normalizedToken = safeLower(token);
        for (AuditFieldChange change : changes) {
            String path = safeLower(change.caminho());
            if (path.endsWith(normalizedToken) || path.contains(normalizedToken)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeResourceKey(String resource) {
        String normalized = trimToNull(resource);
        if (normalized == null) {
            return null;
        }
        int dotIndex = normalized.indexOf('.');
        return dotIndex < 0 ? normalized : normalized.substring(0, dotIndex);
    }

    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> source) {
            java.util.LinkedHashMap<String, Object> normalized = new java.util.LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : source.entrySet()) {
                normalized.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return normalized;
        }
        return Map.of();
    }

    private String extractString(Map<String, Object> value, String key) {
        if (value == null || key == null || key.isBlank()) {
            return null;
        }
        Object candidate = value.get(key);
        if (candidate == null) {
            return null;
        }
        String text = String.valueOf(candidate).trim();
        return text.isEmpty() ? null : text;
    }

    private String extractFileName(Map<String, Object> technical) {
        String fileName = extractString(technical, "fileName");
        if (fileName != null) {
            return fileName;
        }
        return extractString(technical, "nomeArquivo");
    }

    private List<AuditFieldChange> safeChanges(List<AuditFieldChange> alteracoes) {
        return alteracoes == null ? List.of() : alteracoes;
    }

    private String normalizeAction(String action) {
        if (action == null || action.isBlank()) {
            return "ATUALIZAR";
        }
        String normalized = action.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "POST", "CREATE", "CRIAR", AuditActions.INVITE_CREATED, AuditActions.REGISTER_COMPLETED, AuditActions.SUPERADMIN_BOOTSTRAPPED -> "CRIAR";
            case "PUT", "PATCH", "UPDATE", "ATUALIZAR", AuditActions.USER_UPDATED, AuditActions.INVITE_REISSUED, AuditActions.INVITE_CANCELLED, AuditActions.INVITE_VALIDATED -> "ATUALIZAR";
            case "DELETE", "EXCLUIR" -> "EXCLUIR";
            case "LOGIN", AuditActions.LOGIN_SUCCESS, AuditActions.LOGIN_FAILED -> "LOGIN";
            case "LOGOUT" -> "LOGOUT";
            case "ERRO", "ERROR" -> "ERRO";
            default -> normalized;
        };
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String upper(String value) {
        return value == null ? null : value.toUpperCase(Locale.ROOT);
    }

    private String limit(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max) + "...";
    }
}
