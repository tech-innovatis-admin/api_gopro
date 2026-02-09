package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ProjectLocationRequestDTO;
import br.com.gopro.api.dtos.ProjectLocationResponseDTO;
import br.com.gopro.api.dtos.ProjectMonthRequestDTO;
import br.com.gopro.api.dtos.ProjectMonthResponseDTO;
import br.com.gopro.api.dtos.ProjectPartnerRequestDTO;
import br.com.gopro.api.dtos.ProjectPartnerResponseDTO;
import br.com.gopro.api.dtos.ProjectStatusCategoryRequestDTO;
import br.com.gopro.api.dtos.ProjectStatusCategoryResponseDTO;
import br.com.gopro.api.dtos.ProjectTypeDistributionRequestDTO;
import br.com.gopro.api.dtos.ProjectTypeDistributionResponseDTO;

public interface ProjectAnalyticsService {

    ProjectStatusCategoryResponseDTO getStatusCategoryAnalytics(ProjectStatusCategoryRequestDTO request);

    ProjectTypeDistributionResponseDTO getTypeDistributionAnalytics(ProjectTypeDistributionRequestDTO request);

    ProjectMonthResponseDTO getMonthAnalytics(ProjectMonthRequestDTO request);

    ProjectLocationResponseDTO getLocationAnalytics(ProjectLocationRequestDTO request);

    ProjectPartnerResponseDTO getPartnerAnalytics(ProjectPartnerRequestDTO request);
}
