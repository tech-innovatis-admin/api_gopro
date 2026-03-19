package br.com.gopro.api.config;

import br.com.gopro.api.enums.UserRoleEnum;

public record AuthenticatedUserPrincipal(
        Long id,
        String email,
        UserRoleEnum role
) {
}
