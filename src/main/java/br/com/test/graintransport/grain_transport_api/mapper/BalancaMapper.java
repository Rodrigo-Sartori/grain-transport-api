package br.com.test.graintransport.grain_transport_api.mapper;

import br.com.test.graintransport.grain_transport_api.domain.Balanca;
import br.com.test.graintransport.grain_transport_api.domain.Filial;
import br.com.test.graintransport.grain_transport_api.dto.BalancaRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.BalancaResponseDTO;

public class BalancaMapper {

    private BalancaMapper() {
    }

    public static Balanca toEntity(BalancaRequestDTO dto, Filial filial) {
        return Balanca.builder()
                .codigo(dto.codigo())
                .apiKey(dto.apiKey())
                .filial(filial)
                .build();
    }

    public static BalancaResponseDTO toDTO(Balanca balanca) {
        return new BalancaResponseDTO(
                balanca.getId(),
                balanca.getCodigo(),
                balanca.getFilial().getId(),
                balanca.getFilial().getNome()
        );
    }
}
