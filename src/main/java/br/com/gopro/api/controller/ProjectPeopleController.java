package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectPeopleRequestDTO;
import br.com.gopro.api.dtos.ProjectPeopleResponseDTO;
import br.com.gopro.api.dtos.ProjectPeopleUpdateDTO;
import br.com.gopro.api.service.ProjectPeopleService;
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
@RequestMapping("/project-people")
@RequiredArgsConstructor
@Tag(name = "ProjectPeople", description = "Gerenciamento de vinculos projeto-pessoa")
public class ProjectPeopleController {

    private final ProjectPeopleService projectPeopleService;

    @Operation(summary = "Criar vinculo projeto-pessoa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vinculo criado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<ProjectPeopleResponseDTO> create(@Valid @RequestBody ProjectPeopleRequestDTO dto) {
        ProjectPeopleResponseDTO created = projectPeopleService.createProjectPeople(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar vinculos projeto-pessoa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProjectPeopleResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(projectPeopleService.listAllProjectPeople(page, size));
    }

    @Operation(summary = "Buscar vinculo por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vinculo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vinculo nao encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectPeopleResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(projectPeopleService.findProjectPeopleById(id));
    }

    @Operation(summary = "Atualizar vinculo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vinculo atualizado"),
            @ApiResponse(responseCode = "404", description = "Vinculo nao encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectPeopleResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectPeopleUpdateDTO dto
    ) {
        return ResponseEntity.ok(projectPeopleService.updateProjectPeopleById(id, dto));
    }

    @Operation(summary = "Desativar vinculo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vinculo desativado"),
            @ApiResponse(responseCode = "404", description = "Vinculo nao encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectPeopleService.deleteProjectPeopleById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar vinculo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vinculo reativado")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ProjectPeopleResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(projectPeopleService.restoreProjectPeopleById(id));
    }
}