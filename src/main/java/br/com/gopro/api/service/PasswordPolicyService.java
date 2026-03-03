package br.com.gopro.api.service;

import br.com.gopro.api.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {

    public void validateOrThrow(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException("Senha e obrigatoria");
        }
        if (password.length() < 8) {
            throw new BusinessException("Senha deve ter no minimo 8 caracteres");
        }
        if (!hasUppercase(password)) {
            throw new BusinessException("Senha deve conter letra maiuscula");
        }
        if (!hasLowercase(password)) {
            throw new BusinessException("Senha deve conter letra minuscula");
        }
        if (!hasDigit(password)) {
            throw new BusinessException("Senha deve conter numero");
        }
        if (!hasSymbol(password)) {
            throw new BusinessException("Senha deve conter caractere especial");
        }
    }

    private boolean hasUppercase(String value) {
        return value.chars().anyMatch(Character::isUpperCase);
    }

    private boolean hasLowercase(String value) {
        return value.chars().anyMatch(Character::isLowerCase);
    }

    private boolean hasDigit(String value) {
        return value.chars().anyMatch(Character::isDigit);
    }

    private boolean hasSymbol(String value) {
        return value.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
    }
}
