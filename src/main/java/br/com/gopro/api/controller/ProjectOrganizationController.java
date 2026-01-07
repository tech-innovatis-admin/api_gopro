package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.ProjectOrganizationRequestDTO;
import br.com.gopro.api.dtos.ProjectOrganizationResponseDTO;
import br.com.gopro.api.service.ProjectOrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project_organization")
@RequiredArgsConstructor
public class ProjectOrganizationController {

    private final ProjectOrganizationService projectOrganizationService;

    @PostMapping
    public ResponseEntity<ProjectOrganizationResponseDTO> createProjectOrganization(@Valid @RequestBody ProjectOrganizationRequestDTO dto){
        ProjectOrganizationResponseDTO projectOrganizationCreated = projectOrganizationService.createProjectOrganization(dto);

        return ResponseEntity.status(201).body(projectOrganizationCreated);
    }

    @GetMapping
    public ResponseEntity<List<ProjectOrganizationResponseDTO>> listAllProjectOrganization(){
        return ResponseEntity.ok(projectOrganizationService.listAllProjectOrganization());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectOrganizationResponseDTO> findProjectOrganizationById(@PathVariable Long id){
        return ResponseEntity.ok(projectOrganizationService.findProjectOrganizationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectOrganizationResponseDTO> updateProjectOrganization(
            @PathVariable Long id,
            @Valid @RequestBody ProjectOrganizationRequestDTO dto
    ){
        ProjectOrganizationResponseDTO projectOrganizationUpdated = projectOrganizationService.updateProjectOrganizationById(id, dto);

        return ResponseEntity.ok(projectOrganizationUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjectOrganization(@PathVariable Long id){
        projectOrganizationService.deleteProjectOrganizationById(id);

        return ResponseEntity.noContent().build();
    }
}
