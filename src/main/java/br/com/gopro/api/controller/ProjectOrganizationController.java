package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.ProjectOrganizationRequestDTO;
import br.com.gopro.api.dtos.ProjectOrganizationResponseDTO;
import br.com.gopro.api.service.ProjectOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project_organization")
@RequiredArgsConstructor
@Tag(name = "Project Organization", description = "Gerenciamento de organizações vinculadas a projetos")
public class ProjectOrganizationController {

    private final ProjectOrganizationService projectOrganizationService;

    @Operation(summary = "Criar vínculo de organização com projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vínculo criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProjectOrganizationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<ProjectOrganizationResponseDTO> createProjectOrganization(@Valid @RequestBody ProjectOrganizationRequestDTO dto){
        ProjectOrganizationResponseDTO projectOrganizationCreated = projectOrganizationService.createProjectOrganization(dto);

        return ResponseEntity.status(201).body(projectOrganizationCreated);
    }

    @Operation(summary = "Listar todas as organizações vinculadas a projetos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<ProjectOrganizationResponseDTO>> listAllProjectOrganization(){
        return ResponseEntity.ok(projectOrganizationService.listAllProjectOrganization());
    }

    @Operation(summary = "Buscar vínculo de organização por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vínculo encontrado",
                    content = @Content(schema = @Schema(implementation = ProjectOrganizationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Vínculo não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectOrganizationResponseDTO> findProjectOrganizationById(@PathVariable Long id){
        return ResponseEntity.ok(projectOrganizationService.findProjectOrganizationById(id));
    }

    @Operation(summary = "Atualizar vínculo de organização com projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vínculo atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProjectOrganizationResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Vínculo não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectOrganizationResponseDTO> updateProjectOrganization(
            @PathVariable Long id,
            @Valid @RequestBody ProjectOrganizationRequestDTO dto
    ){
        ProjectOrganizationResponseDTO projectOrganizationUpdated = projectOrganizationService.updateProjectOrganizationById(id, dto);

        return ResponseEntity.ok(projectOrganizationUpdated);
    }

    @Operation(summary = "Excluir vínculo de organização por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vínculo removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Vínculo não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjectOrganization(@PathVariable Long id){
        projectOrganizationService.deleteProjectOrganizationById(id);

        return ResponseEntity.noContent().build();
    }
}
