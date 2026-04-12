package br.com.test.graintransport.grain_transport_api.dto;

import java.math.BigDecimal;

public record GrupoStatDTO(String label, BigDecimal valor, long quantidade) {}
