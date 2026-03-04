package br.com.gopro.api.service.audit;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditSnapshotExtractor {

    public Map<String, Object> extract(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Map<?, ?> map) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                normalized.put(key, normalizeValue(entry.getValue()));
            }
            return normalized;
        }

        Map<String, Object> snapshot = new LinkedHashMap<>();
        for (Field field : source.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(source);
                snapshot.put(field.getName(), normalizeValue(value));
            } catch (IllegalAccessException ignored) {
                snapshot.put(field.getName(), null);
            }
        }
        return snapshot;
    }

    private Object normalizeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>
                || value instanceof UUID
                || value instanceof Temporal) {
            return value;
        }
        if (value instanceof Collection<?> collection) {
            return "lista(" + collection.size() + ")";
        }
        if (value instanceof Map<?, ?> map) {
            return "mapa(" + map.size() + ")";
        }
        Object nestedId = extractNestedId(value);
        if (nestedId != null) {
            return nestedId;
        }
        return String.valueOf(value);
    }

    private Object extractNestedId(Object value) {
        Class<?> type = value.getClass();
        while (type != null && type != Object.class) {
            try {
                Field idField = type.getDeclaredField("id");
                idField.setAccessible(true);
                return idField.get(value);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }
        return null;
    }
}

