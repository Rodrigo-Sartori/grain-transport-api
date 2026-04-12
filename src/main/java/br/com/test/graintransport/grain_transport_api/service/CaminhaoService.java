package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.dto.CaminhaoRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.CaminhaoResponseDTO;
import br.com.test.graintransport.grain_transport_api.mapper.CaminhaoMapper;
import br.com.test.graintransport.grain_transport_api.repository.CaminhaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CaminhaoService {

    private final CaminhaoRepository caminhaoRepository;

    @Transactional(readOnly = true)
    public List<CaminhaoResponseDTO> listar() {
        return caminhaoRepository.findAll().stream().map(CaminhaoMapper::toDTO).toList();
    }

    @Transactional
    public CaminhaoResponseDTO salvar(CaminhaoRequestDTO dto) {
        return CaminhaoMapper.toDTO(caminhaoRepository.save(CaminhaoMapper.toEntity(dto)));
    }
}
