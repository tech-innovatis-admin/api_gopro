package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.UserLookupResponseDTO;
import br.com.gopro.api.service.UserDirectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Consulta basica de usuarios")
@PreAuthorize("isAuthenticated()")
public class UserDirectoryController {

    private final UserDirectoryService userDirectoryService;

    @Operation(summary = "Resolver usuarios por lista de IDs")
    @GetMapping("/lookup")
    public ResponseEntity<List<UserLookupResponseDTO>> lookup(
            @RequestParam(name = "ids", required = false) List<Long> ids
    ) {
        return ResponseEntity.ok(userDirectoryService.lookupByIds(ids));
    }
}

