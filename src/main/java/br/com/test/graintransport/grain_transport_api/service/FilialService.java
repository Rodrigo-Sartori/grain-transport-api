package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.dto.FilialRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.FilialResponseDTO;
import br.com.test.graintransport.grain_transport_api.mapper.FilialMapper;
import br.com.test.graintransport.grain_transport_api.repository.FilialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilialService {

    private final FilialRepository filialRepository;

    @Transactional(readOnly = true)
    public List<FilialResponseDTO> listar() {
        return filialRepository.findAll().stream().map(FilialMapper::toDTO).toList();
    }

    @Transactional
    public FilialResponseDTO salvar(FilialRequestDTO dto) {
        return FilialMapper.toDTO(filialRepository.save(FilialMapper.toEntity(dto)));
    }
}
