package br.com.gopro.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.text.Normalizer;
import java.util.Locale;

public enum ExpensePaidByEnum {
    INNOVATIS,
    EXECUCAO;

    @JsonCreator
    public static ExpensePaidByEnum fromValue(String value) {
        if (value == null) {
            return null;
        }

        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "EMPRESA", "INNOVATIS" -> INNOVATIS;
            case "PARCEIRO", "EXECUCAO" -> EXECUCAO;
            default -> throw new IllegalArgumentException("Valor invalido para ExpensePaidByEnum: " + value);
        };
    }
}
