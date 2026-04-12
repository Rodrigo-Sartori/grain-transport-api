package br.com.test.graintransport.grain_transport_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CaminhaoRequestDTO(
        @NotBlank(message = "placa é obrigatória") String placa,
        @NotNull @Positive(message = "tara deve ser positiva") Double tara
) {}
