package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.dto.TransacaoRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.TransacaoResponseDTO;
import br.com.test.graintransport.grain_transport_api.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cadastro/transacao")
@RequiredArgsConstructor
@Tag(name = "Transações")
public class TransacaoController {

    private final TransacaoService transacaoService;

    @GetMapping("/abertas")
    @Operation(summary = "Listar transações em aberto")
    public ResponseEntity<List<TransacaoResponseDTO>> listarAbertas() {
        return ResponseEntity.ok(transacaoService.listarAbertas());
    }

    @PostMapping
    @Operation(summary = "Abrir transação")
    public ResponseEntity<TransacaoResponseDTO> abrir(@Valid @RequestBody TransacaoRequestDTO dto) {
        return ResponseEntity.ok(transacaoService.abrir(dto));
    }

    @PatchMapping("/{id}/fechar")
    @Operation(summary = "Fechar transação")
    public ResponseEntity<TransacaoResponseDTO> fechar(@PathVariable Long id) {
        return ResponseEntity.ok(transacaoService.fechar(id));
    }
}
