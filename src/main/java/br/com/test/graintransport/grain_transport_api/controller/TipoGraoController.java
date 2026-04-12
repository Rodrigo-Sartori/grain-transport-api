package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.dto.TipoGraoRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.TipoGraoResponseDTO;
import br.com.test.graintransport.grain_transport_api.service.TipoGraoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cadastro/grao")
@RequiredArgsConstructor
@Tag(name = "Tipos de Grão")
public class TipoGraoController {

    private final TipoGraoService tipoGraoService;

    @GetMapping
    @Operation(summary = "Listar tipos de grão")
    public ResponseEntity<List<TipoGraoResponseDTO>> listar() {
        return ResponseEntity.ok(tipoGraoService.listar());
    }

    @PostMapping
    @Operation(summary = "Cadastrar tipo de grão")
    public ResponseEntity<TipoGraoResponseDTO> criar(@Valid @RequestBody TipoGraoRequestDTO dto) {
        return ResponseEntity.ok(tipoGraoService.salvar(dto));
    }
}
