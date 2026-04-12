package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.dto.TipoGraoRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.TipoGraoResponseDTO;
import br.com.test.graintransport.grain_transport_api.mapper.TipoGraoMapper;
import br.com.test.graintransport.grain_transport_api.repository.TipoGraoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoGraoService {

    private final TipoGraoRepository tipoGraoRepository;

    @Transactional(readOnly = true)
    public List<TipoGraoResponseDTO> listar() {
        return tipoGraoRepository.findAll().stream().map(TipoGraoMapper::toDTO).toList();
    }

    @Transactional
    public TipoGraoResponseDTO salvar(TipoGraoRequestDTO dto) {
        return TipoGraoMapper.toDTO(tipoGraoRepository.save(TipoGraoMapper.toEntity(dto)));
    }
}
