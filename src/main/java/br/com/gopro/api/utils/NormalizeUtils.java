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

    public static boolean isValidCpf(String cpf) {

        String normalized = normalizeCpf(cpf);

        if (normalized == null || normalized.length() != 11) {
            return false;
        }

        if (normalized.matches("(\\d)\\1{10}")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int digit = Character.getNumericValue(normalized.charAt(i));
            sum += digit * (10 - i);
        }

        int firstCheckDigit = 11 - (sum % 11);
        if (firstCheckDigit >= 10) {
            firstCheckDigit = 0;
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            int digit = Character.getNumericValue(normalized.charAt(i));
            sum += digit * (11 - i);
        }

        int secondCheckDigit = 11 - (sum % 11);
        if (secondCheckDigit >= 10) {
            secondCheckDigit = 0;
        }

        // 5. Compara com os dígitos informados
        return firstCheckDigit == Character.getNumericValue(normalized.charAt(9))
                && secondCheckDigit == Character.getNumericValue(normalized.charAt(10));
    }
}
