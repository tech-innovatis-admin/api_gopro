package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@Valid @RequestBody ProjectRequestDTO dto) {
        ProjectResponseDTO projectCreated =projectService.createProject(dto);
        return ResponseEntity.status(201).body(projectCreated);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> listAllProjects() {
        return ResponseEntity.ok(projectService.listAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> listProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.listProjectById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProjectById(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequestDTO dto
    ) {
        ProjectResponseDTO projectUpdated = projectService.updateProject(id, dto);
        return ResponseEntity.ok(projectUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
