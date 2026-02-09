package br.com.gopro.api.utils;

import java.text.Normalizer;
import java.util.Locale;

public class NormalizeUtils {

    private NormalizeUtils() {}

    public static String normalizeToNumbers(String value) {
        if (value == null) return null;
        return value.replaceAll("\\D", "");
    }

    public static String normalizeCnpj(String cnpj) {
        return normalizeToNumbers(cnpj);
    }

    public static String normalizeCpf(String cpf) {
        return normalizeToNumbers(cpf);
    }

    public static String normalizePhone(String phone) {
        return normalizeToNumbers(phone);
    }

    public static String normalizeZipCode(String zipCode) {
        return normalizeToNumbers(zipCode);
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

    public static String normalizeForSearch(String value) {
        String trimmed = normalizeOrNull(value);
        if (trimmed == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
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

        return firstCheckDigit == Character.getNumericValue(normalized.charAt(9))
                && secondCheckDigit == Character.getNumericValue(normalized.charAt(10));
    }
}
