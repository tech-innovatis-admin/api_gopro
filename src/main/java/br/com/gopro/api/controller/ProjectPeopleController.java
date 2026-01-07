package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.ProjectPeopleRequestDTO;
import br.com.gopro.api.dtos.ProjectPeopleResponseDTO;
import br.com.gopro.api.service.ProjectPeopleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project-people")
@RequiredArgsConstructor
public class ProjectPeopleController {

    private final ProjectPeopleService projectPeopleService;

    @PostMapping
    public ResponseEntity<ProjectPeopleResponseDTO> createProjectPeople(@Valid @RequestBody ProjectPeopleRequestDTO dto) {
        ProjectPeopleResponseDTO projectPeopleCreated = projectPeopleService.createProjectPeople(dto);
        return ResponseEntity.status(201).body(projectPeopleCreated);
    }

    @GetMapping
    public ResponseEntity<List<ProjectPeopleResponseDTO>> listAllProjectPeople(){
        return ResponseEntity.ok(projectPeopleService.listAllProjectPeople());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectPeopleResponseDTO> findProjectPeopleById(@PathVariable Long id){
        return ResponseEntity.ok(projectPeopleService.findProjectPeopleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectPeopleResponseDTO> updateProjectPeople(
            @PathVariable Long id,
            @Valid @RequestBody ProjectPeopleRequestDTO dto
    ){
        ProjectPeopleResponseDTO projectPeopleUpdated = projectPeopleService.updateProjectPeople(id, dto);
        return ResponseEntity.ok(projectPeopleService.updateProjectPeople(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjectPeopleById(@PathVariable Long id){
        projectPeopleService.deleteProjectPeopleById(id);
        return ResponseEntity.noContent().build();
    }
}
