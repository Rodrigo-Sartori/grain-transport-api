package br.com.test.graintransport.grain_transport_api.dto;

import java.math.BigDecimal;
import java.util.List;

public record CustosStatsDTO(
        BigDecimal custoTotalReais,
        BigDecimal custoMedioReais,
        List<GrupoStatDTO> agrupamento
) {}
