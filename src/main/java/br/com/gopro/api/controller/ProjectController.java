package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectLocationRequestDTO;
import br.com.gopro.api.dtos.ProjectLocationResponseDTO;
import br.com.gopro.api.dtos.ProjectMonthRequestDTO;
import br.com.gopro.api.dtos.ProjectMonthResponseDTO;
import br.com.gopro.api.dtos.ProjectPartnerRequestDTO;
import br.com.gopro.api.dtos.ProjectPartnerResponseDTO;
import br.com.gopro.api.dtos.ProjectDashboardResponseDTO;
import br.com.gopro.api.dtos.ProjectStatusCategoryRequestDTO;
import br.com.gopro.api.dtos.ProjectStatusCategoryResponseDTO;
import br.com.gopro.api.dtos.ProjectTypeDistributionRequestDTO;
import br.com.gopro.api.dtos.ProjectTypeDistributionResponseDTO;
import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.dtos.ProjectTotalsDTO;
import br.com.gopro.api.dtos.ProjectUpdateDTO;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import br.com.gopro.api.enums.ProjectGovIfEnum;
import br.com.gopro.api.service.ProjectAnalyticsService;
import br.com.gopro.api.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Gerenciamento de projetos")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectAnalyticsService projectAnalyticsService;

    @Operation(summary = "Criar projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projeto criado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<ProjectResponseDTO> create(@Valid @RequestBody ProjectRequestDTO dto) {
        ProjectResponseDTO created = projectService.createProject(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar projetos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProjectResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(projectService.listAllProjects(page, size));
    }

    @Operation(summary = "Dashboard de contratos com filtros de projetos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard retornado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtro invalido")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<ProjectDashboardResponseDTO> dashboard(
            @Parameter(description = "Filtrar por status do projeto")
            @RequestParam(required = false) ProjectStatusEnum projectStatus,
            @Parameter(description = "Filtrar por tipo de contrato")
            @RequestParam(required = false) ProjectTypeEnum projectType,
            @Parameter(description = "Filtrar por GOV ou IF")
            @RequestParam(required = false) ProjectGovIfEnum projectGovIf,
            @Parameter(description = "Filtrar por execucao pela Innovatis")
            @RequestParam(required = false) Boolean executedByInnovatis,
            @Parameter(description = "Filtrar por mes de referencia (1-12)")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Filtrar por ano de referencia (ex: 2026)")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Filtrar por texto da localidade (cidade, estado ou local de execucao)")
            @RequestParam(required = false) String location,
            @Parameter(description = "Filtrar por parceiro (id primario ou secundario)")
            @RequestParam(required = false) Long partnerId
    ) {
        return ResponseEntity.ok(
                projectService.getDashboard(projectStatus, projectType, projectGovIf, executedByInnovatis, month, year, location, partnerId)
        );
    }

    @Operation(summary = "Filtrar projetos por status e retornar totais por categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtro invalido")
    })
    @GetMapping("/analytics/status-category")
    public ResponseEntity<ProjectStatusCategoryResponseDTO> analyticsByStatusCategory(
            @Valid @ModelAttribute ProjectStatusCategoryRequestDTO request
    ) {
        return ResponseEntity.ok(projectAnalyticsService.getStatusCategoryAnalytics(request));
    }

    @Operation(summary = "Filtrar por categoria e retornar valor total e percentual por tipo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtro invalido")
    })
    @GetMapping("/analytics/type-distribution")
    public ResponseEntity<ProjectTypeDistributionResponseDTO> analyticsByTypeDistribution(
            @Valid @ModelAttribute ProjectTypeDistributionRequestDTO request
    ) {
        return ResponseEntity.ok(projectAnalyticsService.getTypeDistributionAnalytics(request));
    }

    @Operation(summary = "Filtrar contratos por mes e ano de referencia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtro invalido")
    })
    @GetMapping("/analytics/month")
    public ResponseEntity<ProjectMonthResponseDTO> analyticsByMonth(
            @Valid @ModelAttribute ProjectMonthRequestDTO request
    ) {
        return ResponseEntity.ok(projectAnalyticsService.getMonthAnalytics(request));
    }

    @Operation(summary = "Filtrar contratos por localidade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtro invalido")
    })
    @GetMapping("/analytics/location")
    public ResponseEntity<ProjectLocationResponseDTO> analyticsByLocation(
            @Valid @ModelAttribute ProjectLocationRequestDTO request
    ) {
        return ResponseEntity.ok(projectAnalyticsService.getLocationAnalytics(request));
    }

    @Operation(summary = "Filtrar contratos por parceiro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtro invalido"),
            @ApiResponse(responseCode = "404", description = "Parceiro nao encontrado")
    })
    @GetMapping("/analytics/partner")
    public ResponseEntity<ProjectPartnerResponseDTO> analyticsByPartner(
            @Valid @ModelAttribute ProjectPartnerRequestDTO request
    ) {
        return ResponseEntity.ok(projectAnalyticsService.getPartnerAnalytics(request));
    }

    @Operation(summary = "Buscar projeto por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projeto encontrado"),
            @ApiResponse(responseCode = "404", description = "Projeto nao encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.findProjectById(id));
    }

    @Operation(summary = "Atualizar projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projeto atualizado"),
            @ApiResponse(responseCode = "404", description = "Projeto nao encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateDTO dto
    ) {
        return ResponseEntity.ok(projectService.updateProjectById(id, dto));
    }

    @Operation(summary = "Desativar projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Projeto desativado"),
            @ApiResponse(responseCode = "404", description = "Projeto nao encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.deleteProjectById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projeto reativado")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ProjectResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.restoreProjectById(id));
    }

    @Operation(summary = "Totais do projeto (receitas, pagos, reservados e saldos)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Totais calculados"),
            @ApiResponse(responseCode = "404", description = "Projeto nao encontrado")
    })
    @GetMapping("/{id}/totals")
    public ResponseEntity<ProjectTotalsDTO> totals(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectTotals(id));
    }
}
