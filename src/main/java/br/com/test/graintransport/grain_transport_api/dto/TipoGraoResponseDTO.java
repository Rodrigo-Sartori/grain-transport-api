package br.com.test.graintransport.grain_transport_api.dto;

import java.math.BigDecimal;

public record TipoGraoResponseDTO(Long id, String nome, BigDecimal precoPorTonelada, Double estoqueToneladas) {}
