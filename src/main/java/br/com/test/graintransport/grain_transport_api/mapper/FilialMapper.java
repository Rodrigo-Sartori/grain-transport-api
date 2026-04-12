package br.com.test.graintransport.grain_transport_api.mapper;

import br.com.test.graintransport.grain_transport_api.domain.Filial;
import br.com.test.graintransport.grain_transport_api.dto.FilialRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.FilialResponseDTO;

public class FilialMapper {

    private FilialMapper() {}

    public static Filial toEntity(FilialRequestDTO dto) {
        return Filial.builder()
                .nome(dto.nome())
                .cidade(dto.cidade())
                .build();
    }

    public static FilialResponseDTO toDTO(Filial filial) {
        return new FilialResponseDTO(filial.getId(), filial.getNome(), filial.getCidade());
    }
}
