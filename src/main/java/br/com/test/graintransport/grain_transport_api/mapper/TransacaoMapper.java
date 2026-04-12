package br.com.test.graintransport.grain_transport_api.mapper;

import br.com.test.graintransport.grain_transport_api.domain.TransacaoTransporte;
import br.com.test.graintransport.grain_transport_api.dto.TransacaoResponseDTO;

public class TransacaoMapper {

    private TransacaoMapper() {}

    public static TransacaoResponseDTO toDTO(TransacaoTransporte t) {
        return new TransacaoResponseDTO(
                t.getId(),
                t.getCaminhao().getId(),
                t.getCaminhao().getPlaca(),
                t.getTipoGrao().getNome(),
                t.getFilial().getNome(),
                t.getIniciadaEm(),
                t.getFinalizadaEm()
        );
    }
}
