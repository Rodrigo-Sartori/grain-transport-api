package br.com.test.graintransport.grain_transport_api.dto;

import java.math.BigDecimal;
import java.util.List;

public record LucroStatsDTO(
        BigDecimal lucroEstimadoTotalReais,
        BigDecimal margemMediaPercent,
        List<GrupoStatDTO> agrupamento
) {}
