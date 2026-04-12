package br.com.test.graintransport.grain_transport_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PesagemResponseDTO {

    private Long id;
    private Long transacaoId;
    private String placaCaminhao;
    private String tipoGrao;
    private String filial;
    private Double pesoBruto;
    private Double tara;
    private Double pesoLiquido;
    private BigDecimal custoCarga;
    private BigDecimal margemAplicada;
    private LocalDateTime pesadoEm;
    private LocalDateTime criadoEm;
}
