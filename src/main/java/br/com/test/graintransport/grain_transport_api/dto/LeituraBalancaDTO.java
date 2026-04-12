package br.com.test.graintransport.grain_transport_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeituraBalancaDTO {

    @NotNull(message = "id da balança é obrigatório")
    private Long id;

    @NotNull(message = "plate é obrigatório")
    private String plate;

    @NotNull(message = "weight é obrigatório")
    @Positive(message = "weight deve ser positivo")
    private Double weight;
}
