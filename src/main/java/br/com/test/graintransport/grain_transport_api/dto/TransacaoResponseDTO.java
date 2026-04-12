package br.com.test.graintransport.grain_transport_api.dto;

import java.time.LocalDateTime;

public record TransacaoResponseDTO(
        Long id,
        Long caminhaoId,
        String placa,
        String tipoGrao,
        String filial,
        LocalDateTime iniciadaEm,
        LocalDateTime finalizadaEm
) {}
