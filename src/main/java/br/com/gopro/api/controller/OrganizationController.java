package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.OrganizationRequestDTO;
import br.com.gopro.api.dtos.OrganizationResponseDTO;
import br.com.gopro.api.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<OrganizationResponseDTO> createOrganization(@Valid @RequestBody OrganizationRequestDTO dto) {
        OrganizationResponseDTO organizationCreated = organizationService.createOrganization(dto);
        return ResponseEntity.status(201).body(organizationCreated);
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponseDTO>> listAllOrganizations() {
        return ResponseEntity.ok(organizationService.listAllOrganization());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> listOrganizationById(@PathVariable Long id) {
        return ResponseEntity.ok(organizationService.listOrganizationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequestDTO dto
    ){
        OrganizationResponseDTO organizationUpdated = organizationService.updateOrganization(id, dto);
        return ResponseEntity.ok(organizationUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }
}
