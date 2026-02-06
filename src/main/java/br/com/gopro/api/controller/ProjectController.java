package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.dtos.ProjectUpdateDTO;
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
}