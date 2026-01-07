package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.ProjectPeopleRequestDTO;
import br.com.gopro.api.dtos.ProjectPeopleResponseDTO;
import br.com.gopro.api.service.ProjectPeopleService;
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
@RequestMapping("/project-people")
@RequiredArgsConstructor
@Tag(name = "Project People", description = "Gerenciamento de pessoas vinculadas a projetos")
public class ProjectPeopleController {

    private final ProjectPeopleService projectPeopleService;

    @Operation(summary = "Criar vínculo de pessoa com projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vínculo criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProjectPeopleResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<ProjectPeopleResponseDTO> createProjectPeople(@Valid @RequestBody ProjectPeopleRequestDTO dto) {
        ProjectPeopleResponseDTO projectPeopleCreated = projectPeopleService.createProjectPeople(dto);
        return ResponseEntity.status(201).body(projectPeopleCreated);
    }

    @Operation(summary = "Listar todos os vínculos de pessoas com projetos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<ProjectPeopleResponseDTO>> listAllProjectPeople(){
        return ResponseEntity.ok(projectPeopleService.listAllProjectPeople());
    }

    @Operation(summary = "Buscar vínculo por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vínculo encontrado",
                    content = @Content(schema = @Schema(implementation = ProjectPeopleResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Vínculo não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectPeopleResponseDTO> findProjectPeopleById(@PathVariable Long id){
        return ResponseEntity.ok(projectPeopleService.findProjectPeopleById(id));
    }

    @Operation(summary = "Atualizar vínculo de pessoa com projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vínculo atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProjectPeopleResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Vínculo não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectPeopleResponseDTO> updateProjectPeople(
            @PathVariable Long id,
            @Valid @RequestBody ProjectPeopleRequestDTO dto
    ){
        ProjectPeopleResponseDTO projectPeopleUpdated = projectPeopleService.updateProjectPeople(id, dto);
        return ResponseEntity.ok(projectPeopleService.updateProjectPeople(id, dto));
    }

    @Operation(summary = "Excluir vínculo por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vínculo removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Vínculo não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjectPeopleById(@PathVariable Long id){
        projectPeopleService.deleteProjectPeopleById(id);
        return ResponseEntity.noContent().build();
    }
}
