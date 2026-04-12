package br.com.test.graintransport.grain_transport_api.mapper;

import br.com.test.graintransport.grain_transport_api.domain.Caminhao;
import br.com.test.graintransport.grain_transport_api.dto.CaminhaoRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.CaminhaoResponseDTO;

public class CaminhaoMapper {

    private CaminhaoMapper() {}

    public static Caminhao toEntity(CaminhaoRequestDTO dto) {
        return Caminhao.builder()
                .placa(dto.placa())
                .tara(dto.tara())
                .build();
    }

    public static CaminhaoResponseDTO toDTO(Caminhao caminhao) {
        return new CaminhaoResponseDTO(caminhao.getId(), caminhao.getPlaca(), caminhao.getTara());
    }
}
