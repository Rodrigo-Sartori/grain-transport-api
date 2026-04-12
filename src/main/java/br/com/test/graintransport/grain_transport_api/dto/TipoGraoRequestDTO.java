package br.com.test.graintransport.grain_transport_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TipoGraoRequestDTO(
        @NotBlank(message = "nome é obrigatório") String nome,
        @NotNull @Positive(message = "preço deve ser positivo") BigDecimal precoPorTonelada,
        @NotNull @Positive(message = "estoque deve ser positivo") Double estoqueToneladas
) {
}
