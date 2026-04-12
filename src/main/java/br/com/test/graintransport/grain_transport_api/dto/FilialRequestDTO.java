package br.com.test.graintransport.grain_transport_api.dto;

import jakarta.validation.constraints.NotBlank;

public record FilialRequestDTO(
        @NotBlank(message = "nome é obrigatório") String nome,
        @NotBlank(message = "cidade é obrigatória") String cidade
) {}
