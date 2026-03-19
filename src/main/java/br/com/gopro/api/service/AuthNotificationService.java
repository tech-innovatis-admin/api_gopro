package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.dtos.AuthNotificationsReadResponseDTO;
import br.com.gopro.api.dtos.AuthNotificationResponseDTO;

import java.util.List;

public interface AuthNotificationService {

    List<AuthNotificationResponseDTO> listNotifications(AuthenticatedUserPrincipal principal, int size);

    AuthNotificationsReadResponseDTO markAllAsRead(AuthenticatedUserPrincipal principal);
}
