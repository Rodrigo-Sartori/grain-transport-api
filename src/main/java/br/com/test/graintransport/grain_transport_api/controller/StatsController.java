package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.domain.GroupBy;
import br.com.test.graintransport.grain_transport_api.dto.CustosStatsDTO;
import br.com.test.graintransport.grain_transport_api.dto.LucroStatsDTO;
import br.com.test.graintransport.grain_transport_api.dto.PesagensStatsDTO;
import br.com.test.graintransport.grain_transport_api.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Estatísticas", description = "Endpoints analíticos com filtro unificado. Todos os parâmetros são opcionais e combinam-se livremente.")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/pesagens")
    @Operation(
            summary = "Estatísticas de pesagens",
            description = "Retorna totais de peso bruto/líquido e ranking por dimensão (filial, caminhão, grão ou período)."
    )
    public PesagensStatsDTO pesagens(
            @Parameter(description = "Filtrar por filial") @RequestParam(required = false) Long filialId,
            @Parameter(description = "Filtrar por caminhão") @RequestParam(required = false) Long caminhaoId,
            @Parameter(description = "Filtrar por grão") @RequestParam(required = false) Long tipoGraoId,
            @Parameter(description = "Data inicial (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @Parameter(description = "Agrupar por: FILIAL | CAMINHAO | GRAO | PERIODO (padrão: FILIAL)")
            @RequestParam(defaultValue = "FILIAL") GroupBy groupBy
    ) {
        return statsService.pesagens(filialId, caminhaoId, tipoGraoId, inicio, fim, groupBy);
    }

    @GetMapping("/custos")
    @Operation(
            summary = "Estatísticas de custos de compra",
            description = "Retorna custo total e médio das pesagens, com ranking por dimensão escolhida."
    )
    public CustosStatsDTO custos(
            @RequestParam(required = false) Long filialId,
            @RequestParam(required = false) Long caminhaoId,
            @RequestParam(required = false) Long tipoGraoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "FILIAL") GroupBy groupBy
    ) {
        return statsService.custos(filialId, caminhaoId, tipoGraoId, inicio, fim, groupBy);
    }

    @GetMapping("/lucros")
    @Operation(
            summary = "Lucros possíveis estimados",
            description = "Estimativa de lucro baseada na margem aplicada sobre o custo de cada pesagem. Fórmula: custoCarga × margemAplicada."
    )
    public LucroStatsDTO lucros(
            @RequestParam(required = false) Long filialId,
            @RequestParam(required = false) Long caminhaoId,
            @RequestParam(required = false) Long tipoGraoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(defaultValue = "FILIAL") GroupBy groupBy
    ) {
        return statsService.lucros(filialId, caminhaoId, tipoGraoId, inicio, fim, groupBy);
    }
}
