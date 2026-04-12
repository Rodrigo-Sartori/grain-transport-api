package br.com.test.graintransport.grain_transport_api.mapper;

import br.com.test.graintransport.grain_transport_api.domain.TipoGrao;
import br.com.test.graintransport.grain_transport_api.dto.TipoGraoRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.TipoGraoResponseDTO;

public class TipoGraoMapper {

    private TipoGraoMapper() {}

    public static TipoGrao toEntity(TipoGraoRequestDTO dto) {
        return TipoGrao.builder()
                .nome(dto.nome())
                .precoPorTonelada(dto.precoPorTonelada())
                .estoqueToneladas(dto.estoqueToneladas())
                .build();
    }

    public static TipoGraoResponseDTO toDTO(TipoGrao grao) {
        return new TipoGraoResponseDTO(grao.getId(), grao.getNome(), grao.getPrecoPorTonelada(), grao.getEstoqueToneladas());
    }
}
