package br.com.gopro.api.service.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuditSensitiveDataMasker {

    private static final String MASK = "***";
    private static final Set<String> SENSITIVE_TOKENS = Set.of(
            "password",
            "senha",
            "token",
            "authorization",
            "api_key",
            "apikey",
            "secret",
            "cpf",
            "cnpj",
            "documento",
            "invite",
            "sha256",
            "hash"
    );

    private final ObjectMapper objectMapper;

    public JsonNode toMaskedNode(Object rawValue) {
        JsonNode base = toNode(rawValue);
        return maskNode(base, "");
    }

    public List<AuditFieldChange> maskChanges(List<AuditFieldChange> changes) {
        if (changes == null || changes.isEmpty()) {
            return List.of();
        }
        return changes.stream()
                .map(change -> {
                    String caminho = change.caminho();
                    Object de = isSensitivePath(caminho) ? MASK : change.de();
                    Object para = isSensitivePath(caminho) ? MASK : change.para();
                    return new AuditFieldChange(caminho, de, para, change.tipo());
                })
                .toList();
    }

    private JsonNode toNode(Object rawValue) {
        if (rawValue == null) {
            return NullNode.getInstance();
        }
        if (rawValue instanceof JsonNode node) {
            return node.deepCopy();
        }
        if (rawValue instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.isEmpty()) {
                return objectMapper.valueToTree(text);
            }
            try {
                return objectMapper.readTree(trimmed);
            } catch (Exception ignored) {
                return objectMapper.valueToTree(text);
            }
        }
        return objectMapper.valueToTree(rawValue);
    }

    private JsonNode maskNode(JsonNode node, String path) {
        if (node == null || node.isNull()) {
            return NullNode.getInstance();
        }

        if (node.isObject()) {
            ObjectNode source = (ObjectNode) node;
            ObjectNode masked = objectMapper.createObjectNode();
            Iterator<String> fields = source.fieldNames();
            while (fields.hasNext()) {
                String field = fields.next();
                String nextPath = path.isEmpty() ? field : path + "." + field;
                if (isSensitivePath(nextPath)) {
                    masked.put(field, MASK);
                    continue;
                }
                masked.set(field, maskNode(source.get(field), nextPath));
            }
            return masked;
        }

        if (node.isArray()) {
            ArrayNode masked = objectMapper.createArrayNode();
            ArrayNode source = (ArrayNode) node;
            for (int i = 0; i < source.size(); i++) {
                String nextPath = path + "[" + i + "]";
                masked.add(maskNode(source.get(i), nextPath));
            }
            return masked;
        }

        if (isSensitivePath(path)) {
            return objectMapper.valueToTree(MASK);
        }
        return node.deepCopy();
    }

    private boolean isSensitivePath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String normalized = path.toLowerCase(Locale.ROOT);
        for (String token : SENSITIVE_TOKENS) {
            if (normalized.contains(token)) {
                return true;
            }
        }
        return false;
    }
}

