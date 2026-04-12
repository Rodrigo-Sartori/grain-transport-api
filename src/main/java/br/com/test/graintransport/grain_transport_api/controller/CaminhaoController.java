package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.dto.CaminhaoRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.CaminhaoResponseDTO;
import br.com.test.graintransport.grain_transport_api.service.CaminhaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cadastro/caminhao")
@RequiredArgsConstructor
@Tag(name = "Caminhões")
public class CaminhaoController {

    private final CaminhaoService caminhaoService;

    @GetMapping
    @Operation(summary = "Listar caminhões")
    public ResponseEntity<List<CaminhaoResponseDTO>> listar() {
        return ResponseEntity.ok(caminhaoService.listar());
    }

    @PostMapping
    @Operation(summary = "Cadastrar caminhão")
    public ResponseEntity<CaminhaoResponseDTO> criar(@Valid @RequestBody CaminhaoRequestDTO dto) {
        return ResponseEntity.ok(caminhaoService.salvar(dto));
    }
}
