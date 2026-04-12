package br.com.test.graintransport.grain_transport_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BalancaRequestDTO(
        @NotBlank(message = "codigo é obrigatório") String codigo,
        @NotBlank(message = "apiKey é obrigatória") String apiKey,
        @NotNull(message = "filialId é obrigatório") Long filialId
) {
}
