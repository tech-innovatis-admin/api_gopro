package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.PeopleRequestDTO;
import br.com.gopro.api.dtos.PeopleResponseDTO;
import br.com.gopro.api.service.PeopleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/peoples")
@RequiredArgsConstructor
public class PeopleController {

    private final PeopleService peopleService;

    @PostMapping
    public ResponseEntity<PeopleResponseDTO> createPeople(@Valid @RequestBody PeopleRequestDTO dto) {
        PeopleResponseDTO peopleCreated = peopleService.createPeople(dto);
        return ResponseEntity.status(201).body(peopleCreated);
    }

    @GetMapping
    public ResponseEntity<List<PeopleResponseDTO>> listAllPeoples() {
        return ResponseEntity.ok(peopleService.listAllPeoples());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeopleResponseDTO> listPeopleById(@PathVariable Long id) {
        return ResponseEntity.ok(peopleService.listPeopleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PeopleResponseDTO> updatePeople(
            @PathVariable Long id,
            @Valid @RequestBody PeopleRequestDTO dto
    ) {
        PeopleResponseDTO peopleUpdated = peopleService.updatePeople(id, dto);
        return ResponseEntity.ok(peopleUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePeople(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
