package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectCompanyRequestDTO;
import br.com.gopro.api.dtos.ProjectCompanyResponseDTO;
import br.com.gopro.api.dtos.ProjectCompanyUpdateDTO;
import br.com.gopro.api.service.ProjectCompanyService;
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
@RequestMapping({"/project-organizations", "/project_organization"})
@RequiredArgsConstructor
@Tag(name = "ProjectOrganizations", description = "Compatibilidade para vinculos projeto-organizacao")
public class ProjectOrganizationController {

    private final ProjectCompanyService projectCompanyService;

    @Operation(summary = "Criar vinculo projeto-organizacao")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vinculo criado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<ProjectCompanyResponseDTO> create(@Valid @RequestBody ProjectCompanyRequestDTO dto) {
        ProjectCompanyResponseDTO created = projectCompanyService.createProjectCompany(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar vinculos projeto-organizacao")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProjectCompanyResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filtrar por ID do projeto")
            @RequestParam(name = "projectId", required = false) Long projectId
    ) {
        if (projectId != null) {
            return ResponseEntity.ok(projectCompanyService.listProjectCompaniesByProjectId(projectId, page, size));
        }
        return ResponseEntity.ok(projectCompanyService.listAllProjectCompanies(page, size));
    }

    @Operation(summary = "Buscar vinculo por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vinculo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vinculo nao encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectCompanyResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(projectCompanyService.findProjectCompanyById(id));
    }

    @Operation(summary = "Atualizar vinculo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vinculo atualizado"),
            @ApiResponse(responseCode = "404", description = "Vinculo nao encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectCompanyResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectCompanyUpdateDTO dto
    ) {
        return ResponseEntity.ok(projectCompanyService.updateProjectCompanyById(id, dto));
    }

    @Operation(summary = "Desativar vinculo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vinculo desativado"),
            @ApiResponse(responseCode = "404", description = "Vinculo nao encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectCompanyService.deleteProjectCompanyById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar vinculo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vinculo reativado")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ProjectCompanyResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(projectCompanyService.restoreProjectCompanyById(id));
    }
}
