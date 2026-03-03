package br.com.gopro.api.config;

import br.com.gopro.api.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;

public final class SecurityPrincipalUtils {

    private SecurityPrincipalUtils() {
    }

    public static AuthenticatedUserPrincipal require(Authentication authentication) {
        if (authentication == null) {
            throw new UnauthorizedException("Nao autenticado");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUserPrincipal authPrincipal) {
            return authPrincipal;
        }

        throw new UnauthorizedException("Nao autenticado");
    }
}
