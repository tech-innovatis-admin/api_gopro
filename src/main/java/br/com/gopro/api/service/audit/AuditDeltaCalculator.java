package br.com.gopro.api.service.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class AuditDeltaCalculator {

    public List<AuditFieldChange> calculate(JsonNode before, JsonNode after) {
        List<AuditFieldChange> changes = new ArrayList<>();
        diff("", normalize(before), normalize(after), changes);
        return changes;
    }

    private void diff(String path, JsonNode before, JsonNode after, List<AuditFieldChange> collector) {
        if (before.equals(after)) {
            return;
        }

        if (before.isObject() && after.isObject()) {
            Set<String> keys = new LinkedHashSet<>();
            before.fieldNames().forEachRemaining(keys::add);
            after.fieldNames().forEachRemaining(keys::add);
            for (String key : keys) {
                String childPath = path.isBlank() ? key : path + "." + key;
                diff(childPath, normalize(before.get(key)), normalize(after.get(key)), collector);
            }
            return;
        }

        if (before.isArray() && after.isArray()) {
            int max = Math.max(before.size(), after.size());
            for (int i = 0; i < max; i++) {
                String childPath = path + "[" + i + "]";
                JsonNode left = i < before.size() ? before.get(i) : NullNode.getInstance();
                JsonNode right = i < after.size() ? after.get(i) : NullNode.getInstance();
                diff(childPath, normalize(left), normalize(right), collector);
            }
            return;
        }

        if (before.isNull() && !after.isNull()) {
            collector.add(new AuditFieldChange(nonBlankPath(path), null, summarizeValue(after), "ADICIONADO"));
            return;
        }

        if (!before.isNull() && after.isNull()) {
            collector.add(new AuditFieldChange(nonBlankPath(path), summarizeValue(before), null, "REMOVIDO"));
            return;
        }

        collector.add(new AuditFieldChange(nonBlankPath(path), summarizeValue(before), summarizeValue(after), "EDITADO"));
    }

    private JsonNode normalize(JsonNode value) {
        return value == null ? NullNode.getInstance() : value;
    }

    private Object summarizeValue(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isTextual()) {
            return value.asText();
        }
        if (value.isNumber()) {
            return value.numberValue();
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        if (value.isArray()) {
            return "lista(" + value.size() + ")";
        }
        if (value.isObject()) {
            return "objeto";
        }
        return value.asText();
    }

    private String nonBlankPath(String path) {
        return path == null || path.isBlank() ? "registro" : path;
    }
}

