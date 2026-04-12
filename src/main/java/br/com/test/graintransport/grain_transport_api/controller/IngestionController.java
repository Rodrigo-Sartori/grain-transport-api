package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.dto.LeituraBalancaDTO;
import br.com.test.graintransport.grain_transport_api.service.IngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
@Tag(name = "Ingestão", description = "Recebimento de leituras enviadas pelos devices ESP32")
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping
    @Operation(
            summary = "Registrar leitura de balança (ESP32)",
            description = """
                    Recebe o payload do ESP32: `{ "id": 1, "plate": "ABC-1234", "weight": 15420.5 }`.

                    Requer header **X-Api-Key** válido cadastrado para a balança.

                    **Idempotência**: requisições com o mesmo `id` + `plate` dentro de uma janela de 30s
                    retornam **200** sem reprocessamento — retentativas do firmware são seguras.

                    Nova leitura fora da janela retorna **202** e é adicionada ao buffer de estabilização.
                    A pesagem só é persistida quando a janela de N leituras consecutivas apresentar
                    variação abaixo do threshold configurado.
                    """
    )
    public ResponseEntity<Map<String, String>> ingerir(@Valid @RequestBody LeituraBalancaDTO dto) {
        String chave = ingestionService.gerarChaveIdempotencia(dto.getId(), dto.getPlate());

        if (ingestionService.isDuplicata(chave)) {
            log.info("Requisição duplicata ignorada: balanca={}, plate={}", dto.getId(), dto.getPlate());
            return ResponseEntity.ok(Map.of(
                    "status", "duplicata",
                    "plate", dto.getPlate()
            ));
        }

        ingestionService.adicionarLeitura(dto);
        ingestionService.marcarProcessada(chave);

        return ResponseEntity.accepted().body(Map.of(
                "status", "accepted",
                "plate", dto.getPlate(),
                "balanca", String.valueOf(dto.getId())
        ));
    }
}
