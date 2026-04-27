package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.dtos.AuthForgotPasswordRequestDTO;
import br.com.gopro.api.dtos.AuthLoginRequestDTO;
import br.com.gopro.api.dtos.AuthLoginResponseDTO;
import br.com.gopro.api.dtos.AuthResetPasswordRequestDTO;
import br.com.gopro.api.dtos.AuthUserResponseDTO;
import br.com.gopro.api.dtos.MessageResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    AuthLoginResponseDTO login(AuthLoginRequestDTO dto, HttpServletRequest request);

    MessageResponseDTO forgotPassword(AuthForgotPasswordRequestDTO dto, HttpServletRequest request);

    MessageResponseDTO resetPassword(AuthResetPasswordRequestDTO dto, HttpServletRequest request);

    AuthUserResponseDTO me(AuthenticatedUserPrincipal principal);

    AuthUserResponseDTO updateMyAvatar(AuthenticatedUserPrincipal principal, MultipartFile file);
}
