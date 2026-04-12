package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.dto.PesagemResponseDTO;
import br.com.test.graintransport.grain_transport_api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Consulta de pesagens registradas")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/pesagens")
    @Operation(summary = "Listar todas as pesagens")
    public ResponseEntity<List<PesagemResponseDTO>> listarTodas() {
        return ResponseEntity.ok(reportService.listarTodas());
    }

    @GetMapping("/pesagens/transacao/{transacaoId}")
    @Operation(summary = "Listar pesagens por transação")
    public ResponseEntity<List<PesagemResponseDTO>> porTransacao(@PathVariable Long transacaoId) {
        return ResponseEntity.ok(reportService.listarPorTransacao(transacaoId));
    }

    @GetMapping("/pesagens/filial/{filialId}")
    @Operation(summary = "Listar pesagens por filial")
    public ResponseEntity<List<PesagemResponseDTO>> porFilial(@PathVariable Long filialId) {
        return ResponseEntity.ok(reportService.listarPorFilial(filialId));
    }

    @GetMapping("/pesagens/periodo")
    @Operation(
            summary = "Listar pesagens por período",
            description = "Formato: ISO 8601 — ex: 2024-01-01T00:00:00"
    )
    public ResponseEntity<List<PesagemResponseDTO>> porPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(reportService.listarPorPeriodo(inicio, fim));
    }
}
