package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.dto.FilialRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.FilialResponseDTO;
import br.com.test.graintransport.grain_transport_api.service.FilialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cadastro/filial")
@RequiredArgsConstructor
@Tag(name = "Filiais")
public class FilialController {

    private final FilialService filialService;

    @GetMapping
    @Operation(summary = "Listar filiais")
    public ResponseEntity<List<FilialResponseDTO>> listar() {
        return ResponseEntity.ok(filialService.listar());
    }

    @PostMapping
    @Operation(summary = "Cadastrar filial")
    public ResponseEntity<FilialResponseDTO> criar(@Valid @RequestBody FilialRequestDTO dto) {
        return ResponseEntity.ok(filialService.salvar(dto));
    }
}
