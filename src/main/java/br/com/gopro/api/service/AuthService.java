package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.dtos.AuthLoginRequestDTO;
import br.com.gopro.api.dtos.AuthLoginResponseDTO;
import br.com.gopro.api.dtos.AuthUserResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthLoginResponseDTO login(AuthLoginRequestDTO dto, HttpServletRequest request);

    AuthUserResponseDTO me(AuthenticatedUserPrincipal principal);
}
