package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.domain.Pesagem;
import br.com.test.graintransport.grain_transport_api.dto.PesagemResponseDTO;
import br.com.test.graintransport.grain_transport_api.repository.PesagemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final PesagemRepository pesagemRepository;

    @Transactional(readOnly = true)
    public List<PesagemResponseDTO> listarPorTransacao(Long transacaoId) {
        return pesagemRepository.findByTransacaoId(transacaoId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PesagemResponseDTO> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return pesagemRepository.findByPeriodo(inicio, fim)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PesagemResponseDTO> listarPorFilial(Long filialId) {
        return pesagemRepository.findByFilialId(filialId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PesagemResponseDTO> listarTodas() {
        return pesagemRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private PesagemResponseDTO toDTO(Pesagem p) {
        return PesagemResponseDTO.builder()
                .id(p.getId())
                .transacaoId(p.getTransacao().getId())
                .placaCaminhao(p.getTransacao().getCaminhao().getPlaca())
                .tipoGrao(p.getTransacao().getTipoGrao().getNome())
                .filial(p.getTransacao().getFilial().getNome())
                .pesoBruto(p.getPesoBruto())
                .tara(p.getTara())
                .pesoLiquido(p.getPesoLiquido())
                .custoCarga(p.getCustoCarga())
                .margemAplicada(p.getMargemAplicada())
                .pesadoEm(p.getPesadoEm())
                .build();
    }
}
