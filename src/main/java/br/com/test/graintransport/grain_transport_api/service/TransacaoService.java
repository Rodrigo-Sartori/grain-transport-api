package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.domain.TransacaoTransporte;
import br.com.test.graintransport.grain_transport_api.dto.TransacaoRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.TransacaoResponseDTO;
import br.com.test.graintransport.grain_transport_api.exception.CaminhaoEmTransporteException;
import br.com.test.graintransport.grain_transport_api.mapper.TransacaoMapper;
import br.com.test.graintransport.grain_transport_api.repository.CaminhaoRepository;
import br.com.test.graintransport.grain_transport_api.repository.FilialRepository;
import br.com.test.graintransport.grain_transport_api.repository.TipoGraoRepository;
import br.com.test.graintransport.grain_transport_api.repository.TransacaoTransporteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoTransporteRepository transacaoRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final TipoGraoRepository tipoGraoRepository;
    private final FilialRepository filialRepository;

    @Transactional(readOnly = true)
    public List<TransacaoResponseDTO> listarAbertas() {
        return transacaoRepository.findAllAbertas().stream().map(TransacaoMapper::toDTO).toList();
    }

    @Transactional
    public TransacaoResponseDTO abrir(TransacaoRequestDTO dto) {
        var caminhao = caminhaoRepository.findById(dto.getCaminhaoId())
                .orElseThrow(() -> new EntityNotFoundException("Caminhão não encontrado: " + dto.getCaminhaoId()));
        var tipoGrao = tipoGraoRepository.findById(dto.getTipoGraoId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de grão não encontrado: " + dto.getTipoGraoId()));
        var filial = filialRepository.findById(dto.getFilialId())
                .orElseThrow(() -> new EntityNotFoundException("Filial não encontrada: " + dto.getFilialId()));
        var transacaoParaCaminhao = transacaoRepository.findAbertasByCaminhao(caminhao.getId());
        if (!transacaoParaCaminhao.isEmpty())
            throw new CaminhaoEmTransporteException("Não é permitido cadastrar uma transação para caminhão com placa %s pois está em viagem".formatted(caminhao.getPlaca()));

        var transacao = TransacaoTransporte.builder()
                .caminhao(caminhao).tipoGrao(tipoGrao).filial(filial)
                .iniciadaEm(LocalDateTime.now())
                .build();

        return TransacaoMapper.toDTO(transacaoRepository.save(transacao));
    }

    @Transactional
    public TransacaoResponseDTO fechar(Long id) {
        var transacao = transacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transação não encontrada: " + id));
        transacao.setFinalizadaEm(LocalDateTime.now());
        return TransacaoMapper.toDTO(transacaoRepository.save(transacao));
    }
}
