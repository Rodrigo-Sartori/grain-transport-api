package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.dto.BalancaRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.BalancaResponseDTO;
import br.com.test.graintransport.grain_transport_api.mapper.BalancaMapper;
import br.com.test.graintransport.grain_transport_api.repository.BalancaRepository;
import br.com.test.graintransport.grain_transport_api.repository.FilialRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BalancaService {

    private final BalancaRepository balancaRepository;
    private final FilialRepository filialRepository;

    @Transactional(readOnly = true)
    public List<BalancaResponseDTO> listar() {
        return balancaRepository.findAll().stream().map(BalancaMapper::toDTO).toList();
    }

    @Transactional
    public BalancaResponseDTO salvar(BalancaRequestDTO dto) {
        var filial = filialRepository.findById(dto.filialId())
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada: " + dto.filialId()));
        return BalancaMapper.toDTO(balancaRepository.save(BalancaMapper.toEntity(dto, filial)));
    }
}
