package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.dtos.*;
import br.com.gopro.api.enums.AllowedRegistrationStatusEnum;
import jakarta.servlet.http.HttpServletRequest;

public interface AllowedRegistrationService {

    AllowedRegistrationResponseDTO createInvite(
            AllowedRegistrationCreateRequestDTO dto,
            AuthenticatedUserPrincipal actor,
            HttpServletRequest request
    );

    PageResponseDTO<AllowedRegistrationResponseDTO> listInvites(
            AllowedRegistrationStatusEnum status,
            int page,
            int size
    );

    AllowedRegistrationResponseDTO cancelInvite(
            Long id,
            AuthenticatedUserPrincipal actor,
            HttpServletRequest request
    );

    AllowedRegistrationResponseDTO reissueInvite(
            Long id,
            AllowedRegistrationReissueRequestDTO dto,
            AuthenticatedUserPrincipal actor,
            HttpServletRequest request
    );

    AllowedRegistrationValidationResponseDTO validateInviteToken(String token, HttpServletRequest request);

    RegisterCompleteResponseDTO completeRegistration(RegisterCompleteRequestDTO dto, HttpServletRequest request);
}
