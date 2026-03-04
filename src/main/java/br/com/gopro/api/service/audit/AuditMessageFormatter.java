package br.com.gopro.api.service.audit;

import br.com.gopro.api.enums.AuditScopeEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class AuditMessageFormatter {

    public AuditMessage format(AuditEventRequest request) {
        if (request == null) {
            return new AuditMessage("Acao registrada", "Uma acao foi registrada na trilha de auditoria.");
        }

        AuditScopeEnum scope = request.getTipoAuditoria();
        if (scope == AuditScopeEnum.CONTRACTS) {
            return formatContracts(request);
        }
        if (scope == AuditScopeEnum.USERS) {
            return formatUsers(request);
        }
        return formatSystem(request);
    }

    private AuditMessage formatContracts(AuditEventRequest request) {
        String contractLabel = request.getEntidadeId() != null
                ? "Contrato #" + request.getEntidadeId()
                : "Contrato";
        String aba = nonBlankOrFallback(request.getAba(), "Geral");
        String acao = normalizeAction(request.getAcao());
        List<AuditFieldChange> alteracoes = safeChanges(request.getAlteracoes());
        String fileName = extractFileName(request.getDetalhesTecnicos());

        String resumo;
        if ("CRIAR".equals(acao) && fileName != null) {
            resumo = contractLabel + ": Adicionado anexo '" + fileName + "' (Aba " + aba + ")";
        } else if ("EXCLUIR".equals(acao) && fileName != null) {
            resumo = contractLabel + ": Excluido anexo '" + fileName + "' (Aba " + aba + ")";
        } else if ("ATUALIZAR".equals(acao) && !alteracoes.isEmpty()) {
            AuditFieldChange first = alteracoes.get(0);
            resumo = contractLabel + ": " + summarizeContractChange(first, aba);
        } else {
            resumo = switch (acao) {
                case "CRIAR" -> contractLabel + ": Registro criado na aba " + aba;
                case "EXCLUIR" -> contractLabel + ": Registro excluido da aba " + aba;
                default -> contractLabel + ": Informacoes atualizadas na aba " + aba;
            };
        }

        StringBuilder descricao = new StringBuilder();
        descricao.append("Modulo: Contratos. Tela: ").append(nonBlankOrFallback(request.getFeature(), "Gestao de contratos")).append(". ");
        descricao.append("A acao foi registrada na aba ").append(aba).append(". ");
        if (!alteracoes.isEmpty()) {
            descricao.append("Campos alterados: ").append(joinChangedPaths(alteracoes)).append(".");
        } else {
            descricao.append("Sem alteracoes detalhadas de campo.");
        }

        return new AuditMessage(limit(resumo, 500), limit(descricao.toString(), 2000));
    }

    private AuditMessage formatUsers(AuditEventRequest request) {
        String acao = normalizeAction(request.getAcao());
        List<AuditFieldChange> alteracoes = safeChanges(request.getAlteracoes());
        String userRef = resolveUserReference(request);

        String resumo;
        if ("LOGIN".equals(acao)) {
            resumo = userRef + ": Login realizado";
        } else if ("LOGOUT".equals(acao)) {
            resumo = userRef + ": Logout realizado";
        } else if ("ERRO".equals(acao)) {
            resumo = userRef + ": Falha em operacao de usuario";
        } else if ("ATUALIZAR".equals(acao) && !alteracoes.isEmpty()) {
            AuditFieldChange first = alteracoes.get(0);
            resumo = userRef + ": " + summarizeUserChange(first);
        } else if ("CRIAR".equals(acao)) {
            resumo = userRef + ": Cadastro criado";
        } else if ("EXCLUIR".equals(acao)) {
            resumo = userRef + ": Cadastro removido";
        } else {
            resumo = userRef + ": Cadastro atualizado";
        }

        StringBuilder descricao = new StringBuilder();
        descricao.append("Modulo: ").append(nonBlankOrFallback(request.getModulo(), "Usuarios")).append(". ");
        descricao.append("Tela: ").append(nonBlankOrFallback(request.getFeature(), "Gestao de usuarios")).append(". ");
        if (!alteracoes.isEmpty()) {
            descricao.append("Alteracoes identificadas: ").append(joinChangedPaths(alteracoes)).append(".");
        } else {
            descricao.append("Acao registrada sem alteracoes detalhadas de campo.");
        }

        return new AuditMessage(limit(resumo, 500), limit(descricao.toString(), 2000));
    }

    private AuditMessage formatSystem(AuditEventRequest request) {
        String acao = normalizeAction(request.getAcao());
        String modulo = nonBlankOrFallback(request.getModulo(), "Sistema");
        String feature = nonBlankOrFallback(request.getFeature(), "Operacao interna");

        String resumo = switch (acao) {
            case "LOGIN" -> "Acesso ao sistema realizado";
            case "LOGOUT" -> "Saida do sistema realizada";
            case "ERRO" -> "Falha registrada no sistema";
            default -> "Evento registrado no sistema";
        };

        String descricao = "Modulo: " + modulo + ". Tela: " + feature + ". Evento rastreado para governanca e conformidade.";
        return new AuditMessage(limit(resumo, 500), limit(descricao, 2000));
    }

    private String summarizeContractChange(AuditFieldChange change, String aba) {
        String campo = humanize(change.caminho());
        String de = summarizeValue(change.de());
        String para = summarizeValue(change.para());
        return "Campo " + campo + " alterado de " + de + " para " + para + " (Aba " + aba + ")";
    }

    private String summarizeUserChange(AuditFieldChange change) {
        String caminho = change.caminho() != null ? change.caminho().toLowerCase(Locale.ROOT) : "";
        String de = summarizeValue(change.de());
        String para = summarizeValue(change.para());
        if (caminho.endsWith("role")) {
            return "Perfil alterado de " + de + " para " + para;
        }
        if (caminho.endsWith("status")) {
            return "Status alterado de " + de + " para " + para;
        }
        return "Campo " + humanize(change.caminho()) + " alterado de " + de + " para " + para;
    }

    private String resolveUserReference(AuditEventRequest request) {
        Object after = request.getDepois();
        Object before = request.getAntes();
        String fullName = extractString(after, "fullName");
        if (fullName == null) {
            fullName = extractString(before, "fullName");
        }
        if (fullName != null) {
            return "Usuario " + fullName;
        }
        String email = extractString(after, "email");
        if (email == null) {
            email = extractString(before, "email");
        }
        if (email != null) {
            return "Usuario " + email;
        }
        if (request.getEntidadeId() != null) {
            return "Usuario #" + request.getEntidadeId();
        }
        return "Usuario";
    }

    private String extractString(Object value, String key) {
        if (!(value instanceof Map<?, ?> map)) {
            return null;
        }
        Object candidate = map.get(key);
        if (candidate == null) {
            return null;
        }
        String text = String.valueOf(candidate).trim();
        return text.isEmpty() ? null : text;
    }

    private String joinChangedPaths(List<AuditFieldChange> alteracoes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < alteracoes.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(humanize(alteracoes.get(i).caminho()));
            if (i == 4 && alteracoes.size() > 5) {
                builder.append(", outros campos");
                break;
            }
        }
        return builder.toString();
    }

    private String humanize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Campo";
        }
        String noIndexes = raw.replaceAll("\\[(\\d+)]", " $1 ");
        String dotted = noIndexes.replace('.', ' ').replace('_', ' ');
        String spaced = dotted.replaceAll("([a-z])([A-Z])", "$1 $2").trim();
        if (spaced.isBlank()) {
            return "Campo";
        }
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    private String summarizeValue(Object value) {
        if (value == null) {
            return "vazio";
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return "vazio";
        }
        return limit(text, 80);
    }

    private String normalizeAction(String action) {
        if (action == null || action.isBlank()) {
            return "ATUALIZAR";
        }
        String normalized = action.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "POST", "CREATE", "CRIAR", "INVITE_CREATED", "REGISTER_COMPLETED", "SUPERADMIN_BOOTSTRAPPED" -> "CRIAR";
            case "PUT", "PATCH", "UPDATE", "ATUALIZAR", "USER_UPDATED", "INVITE_REISSUED", "INVITE_CANCELLED", "INVITE_VALIDATED" -> "ATUALIZAR";
            case "DELETE", "EXCLUIR" -> "EXCLUIR";
            case "LOGIN", "LOGIN_SUCCESS", "LOGIN_FAILED" -> "LOGIN";
            case "LOGOUT" -> "LOGOUT";
            case "ERRO", "ERROR" -> "ERRO";
            default -> normalized;
        };
    }

    private String extractFileName(Object detalhesTecnicos) {
        if (!(detalhesTecnicos instanceof Map<?, ?> map)) {
            return null;
        }
        Object candidate = map.get("fileName");
        if (candidate == null) {
            candidate = map.get("nomeArquivo");
        }
        if (candidate == null) {
            return null;
        }
        String value = String.valueOf(candidate).trim();
        return value.isEmpty() ? null : value;
    }

    private List<AuditFieldChange> safeChanges(List<AuditFieldChange> alteracoes) {
        return alteracoes == null ? List.of() : alteracoes;
    }

    private String nonBlankOrFallback(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
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

