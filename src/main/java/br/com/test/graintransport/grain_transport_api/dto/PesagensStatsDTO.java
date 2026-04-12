package br.com.test.graintransport.grain_transport_api.dto;

import java.util.List;

public record PesagensStatsDTO(
        long totalPesagens,
        double pesoBrutoTotalKg,
        double pesoLiquidoTotalKg,
        List<GrupoStatDTO> agrupamento
) {}
