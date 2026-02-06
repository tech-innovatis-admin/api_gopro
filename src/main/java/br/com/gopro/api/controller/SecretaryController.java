package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.SecretaryRequestDTO;
import br.com.gopro.api.dtos.SecretaryResponseDTO;
import br.com.gopro.api.dtos.SecretaryUpdateDTO;
import br.com.gopro.api.service.SecretaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/secretaries")
@RequiredArgsConstructor
@Tag(name = "Secretaries", description = "Gerenciamento de secretarias")
public class SecretaryController {

    private final SecretaryService secretaryService;

    @Operation(summary = "Criar secretaria")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Secretaria criada com sucesso",
                    content = @Content(schema = @Schema(implementation = SecretaryResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<SecretaryResponseDTO> createSecretary(@Valid @RequestBody SecretaryRequestDTO dto) {
        SecretaryResponseDTO secretaryCreated = secretaryService.createSecretary(dto);
        return ResponseEntity.status(201).body(secretaryCreated);
    }

    @Operation(summary = "Listar todas as secretarias com paginacao")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<SecretaryResponseDTO>> listAllSecretaries(
            @Parameter(description = "Numero da pagina (comecando em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina (maximo 100)") @RequestParam(defaultValue = "10") int size) {
        PageResponseDTO<SecretaryResponseDTO> secretaries = secretaryService.listAllSecretary(page, size);
        return ResponseEntity.ok(secretaries);
    }

    @Operation(summary = "Buscar secretaria por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Secretaria encontrada",
                    content = @Content(schema = @Schema(implementation = SecretaryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Secretaria nao encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SecretaryResponseDTO> findSecretaryById(@PathVariable Long id) {
        SecretaryResponseDTO secretary = secretaryService.findSecretaryById(id);
        return ResponseEntity.ok(secretary);
    }

    @Operation(summary = "Atualizar secretaria por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Secretaria atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = SecretaryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Secretaria nao encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SecretaryResponseDTO> updateSecretaryById(
            @PathVariable Long id,
            @Valid @RequestBody SecretaryUpdateDTO dto) {
        SecretaryResponseDTO secretaryUpdated = secretaryService.updateSecretaryById(id, dto);
        return ResponseEntity.ok(secretaryUpdated);
    }

    @Operation(summary = "Desativar secretaria por ID (Soft Delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Secretaria desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Secretaria nao encontrada"),
            @ApiResponse(responseCode = "400", description = "Secretaria ja esta inativa")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSecretaryById(@PathVariable Long id) {
        secretaryService.deleteSecretaryById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar secretaria por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Secretaria reativada com sucesso",
                    content = @Content(schema = @Schema(implementation = SecretaryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Secretaria nao encontrada"),
            @ApiResponse(responseCode = "400", description = "Secretaria ja esta ativa")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<SecretaryResponseDTO> restoreSecretaryById(@PathVariable Long id) {
        SecretaryResponseDTO secretaryRestored = secretaryService.restoreSecretaryById(id);
        return ResponseEntity.ok(secretaryRestored);
    }

    @Operation(summary = "Ativar secretaria como cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Secretaria ativada como cliente com sucesso",
                    content = @Content(schema = @Schema(implementation = SecretaryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Secretaria nao encontrada"),
            @ApiResponse(responseCode = "400", description = "Secretaria ja e cliente")
    })
    @PatchMapping("/{id}/activate-client")
    public ResponseEntity<SecretaryResponseDTO> activateSecretaryAsClient(@PathVariable Long id) {
        SecretaryResponseDTO secretaryActivated = secretaryService.activateSecretaryAsClientById(id);
        return ResponseEntity.ok(secretaryActivated);
    }

    @Operation(summary = "Desativar secretaria como cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Secretaria desativada como cliente com sucesso",
                    content = @Content(schema = @Schema(implementation = SecretaryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Secretaria nao encontrada"),
            @ApiResponse(responseCode = "400", description = "Secretaria nao e cliente")
    })
    @PatchMapping("/{id}/deactivate-client")
    public ResponseEntity<SecretaryResponseDTO> deactivateSecretaryAsClient(@PathVariable Long id) {
        SecretaryResponseDTO secretaryDeactivated = secretaryService.desactivateSecretaryAsClientById(id);
        return ResponseEntity.ok(secretaryDeactivated);
    }
}
