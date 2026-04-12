package br.com.test.graintransport.grain_transport_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoRequestDTO {

    @NotNull(message = "ID do caminhão é obrigatório")
    private Long caminhaoId;

    @NotNull(message = "ID do tipo de grão é obrigatório")
    private Long tipoGraoId;

    @NotNull(message = "ID da filial é obrigatório")
    private Long filialId;
}
