package br.com.gopro.api.controller;

import br.com.gopro.api.dtos.CompanyRequestDTO;
import br.com.gopro.api.dtos.CompanyResponseDTO;
import br.com.gopro.api.dtos.CompanyUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.service.CompanyService;
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
@RequestMapping("/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Gerenciamento de empresas")
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "Criar empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PostMapping
    public ResponseEntity<CompanyResponseDTO> create(@Valid @RequestBody CompanyRequestDTO dto) {
        CompanyResponseDTO created = companyService.createCompany(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar empresas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<CompanyResponseDTO>> list(
            @Parameter(description = "Numero da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(companyService.listAllCompanies(page, size));
    }

    @Operation(summary = "Buscar empresa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "404", description = "Empresa nao encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.findCompanyById(id));
    }

    @Operation(summary = "Atualizar empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa atualizada"),
            @ApiResponse(responseCode = "404", description = "Empresa nao encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CompanyUpdateDTO dto
    ) {
        return ResponseEntity.ok(companyService.updateCompanyById(id, dto));
    }

    @Operation(summary = "Desativar empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Empresa desativada"),
            @ApiResponse(responseCode = "404", description = "Empresa nao encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyService.deleteCompanyById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar empresa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa reativada")
    })
    @PatchMapping("/{id}/restore")
    public ResponseEntity<CompanyResponseDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.restoreCompanyById(id));
    }
}