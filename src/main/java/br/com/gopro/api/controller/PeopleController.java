package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PeopleRequestDTO;
import br.com.gopro.api.dtos.PeopleResponseDTO;
import br.com.gopro.api.service.PeopleService;
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
@RequestMapping("/peoples")
@RequiredArgsConstructor
@Tag(name = "People", description = "Gerenciamento de pessoas")
public class PeopleController {

    private final PeopleService peopleService;

    @Operation(summary = "Criar pessoa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pessoa criada com sucesso",
                    content = @Content(schema = @Schema(implementation = PeopleResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<PeopleResponseDTO> createPeople(@Valid @RequestBody PeopleRequestDTO dto) {
        PeopleResponseDTO peopleCreated = peopleService.createPeople(dto);
        return ResponseEntity.status(201).body(peopleCreated);
    }

    @Operation(summary = "Listar todas as pessoas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<PeopleResponseDTO>> listAllPeoples(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(peopleService.listAllPeoples(page, size));
    }

    @Operation(summary = "Buscar pessoa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pessoa encontrada",
                    content = @Content(schema = @Schema(implementation = PeopleResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa nao encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PeopleResponseDTO> listPeopleById(@PathVariable Long id) {
        return ResponseEntity.ok(peopleService.listPeopleById(id));
    }

    @Operation(summary = "Atualizar pessoa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pessoa atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = PeopleResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa nao encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PeopleResponseDTO> updatePeople(
            @PathVariable Long id,
            @Valid @RequestBody PeopleRequestDTO dto
    ) {
        PeopleResponseDTO peopleUpdated = peopleService.updatePeople(id, dto);
        return ResponseEntity.ok(peopleUpdated);
    }

    @Operation(summary = "Excluir pessoa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pessoa removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pessoa nao encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePeople(@PathVariable Long id) {
        peopleService.deletePeople(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar pessoa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pessoa reativada")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<PeopleResponseDTO> restorePeople(@PathVariable Long id) {
        return ResponseEntity.ok(peopleService.restorePeople(id));
    }
}