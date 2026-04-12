package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.dto.BalancaRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.BalancaResponseDTO;
import br.com.test.graintransport.grain_transport_api.service.BalancaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cadastro/balanca")
@RequiredArgsConstructor
@Tag(name = "Balanças")
public class BalancaController {

    private final BalancaService balancaService;

    @GetMapping
    @Operation(summary = "Listar balanças")
    public ResponseEntity<List<BalancaResponseDTO>> listar() {
        return ResponseEntity.ok(balancaService.listar());
    }

    @PostMapping
    @Operation(summary = "Cadastrar balança")
    public ResponseEntity<BalancaResponseDTO> criar(@Valid @RequestBody BalancaRequestDTO dto) {
        return ResponseEntity.ok(balancaService.salvar(dto));
    }
}
