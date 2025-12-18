package br.com.gopro.api.utils;

import java.text.Normalizer;

public class NormalizeUtils {

    private NormalizeUtils(){}

    public static String normalizeToNumbers(String value) {
        if (value == null) return null;
        return value.replaceAll("\\D", "");
    }

    public static String normalizeCnpj(String cnpj) {
        String normalized = normalizeToNumbers(cnpj);
        return normalized;
    }

    public static String normalizeCpf(String cpf) {
        String normalized = normalizeToNumbers(cpf);
        return normalized;
    }

    public static String normalizePhone(String phone) {
        String normalized = normalizeToNumbers(phone);
        return normalized;
    }

    public static String normalizeZipCode(String zipCode) {
        String normalized = normalizeToNumbers(zipCode);
        return normalized;
    }

    public static boolean isNumeric(String value) {
        if (value == null) return false;
        return value.matches("\\d+");
    }

    public static String normalizeOrNull(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
