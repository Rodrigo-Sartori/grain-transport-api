package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.domain.*;
import br.com.test.graintransport.grain_transport_api.repository.PesagemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService")
class ReportServiceTest {

    @Mock PesagemRepository pesagemRepository;
    @InjectMocks ReportService service;

    private Pesagem buildPesagem(Long id) {
        var filial   = new Filial(1L, "Filial Sul", "Maringá");
        var caminhao = new Caminhao(2L, "ABC-1234", 8500.0);
        var grao     = new TipoGrao(3L, "Soja", new BigDecimal("2800.00"), 1500.0);
        var transacao = TransacaoTransporte.builder()
                .id(10L)
                .caminhao(caminhao)
                .tipoGrao(grao)
                .filial(filial)
                .iniciadaEm(LocalDateTime.of(2025, 6, 1, 8, 0))
                .build();

        return Pesagem.builder()
                .id(id)
                .transacao(transacao)
                .balanca(new Balanca(4L, "BAL-001", "chave", filial))
                .pesoBruto(18000.0)
                .tara(8500.0)
                .pesoLiquido(9500.0)
                .custoCarga(new BigDecimal("29260.00"))
                .margemAplicada(new BigDecimal("0.1000"))
                .pesadoEm(LocalDateTime.of(2025, 6, 1, 8, 30))
                .build();
    }

    @Test
    @DisplayName("listarPorTransacao() deve retornar pesagens mapeadas para DTO")
    void listarPorTransacao_deveRetornarDTOs() {
        when(pesagemRepository.findByTransacaoId(10L)).thenReturn(List.of(buildPesagem(1L)));

        var result = service.listarPorTransacao(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransacaoId()).isEqualTo(10L);
        assertThat(result.get(0).getPlacaCaminhao()).isEqualTo("ABC-1234");
        assertThat(result.get(0).getTipoGrao()).isEqualTo("Soja");
        assertThat(result.get(0).getFilial()).isEqualTo("Filial Sul");
        assertThat(result.get(0).getPesoBruto()).isEqualTo(18000.0);
        assertThat(result.get(0).getPesoLiquido()).isEqualTo(9500.0);
        assertThat(result.get(0).getCustoCarga()).isEqualByComparingTo(new BigDecimal("29260.00"));
        assertThat(result.get(0).getMargemAplicada()).isEqualByComparingTo(new BigDecimal("0.1000"));
        verify(pesagemRepository).findByTransacaoId(10L);
    }

    @Test
    @DisplayName("listarPorPeriodo() deve repassar intervalo ao repositório")
    void listarPorPeriodo_deveRepassarIntervalo() {
        var inicio = LocalDateTime.of(2025, 6, 1, 0, 0);
        var fim    = LocalDateTime.of(2025, 6, 30, 23, 59);
        when(pesagemRepository.findByPeriodo(inicio, fim)).thenReturn(List.of(buildPesagem(2L)));

        var result = service.listarPorPeriodo(inicio, fim);

        assertThat(result).hasSize(1);
        verify(pesagemRepository).findByPeriodo(inicio, fim);
    }

    @Test
    @DisplayName("listarPorFilial() deve retornar pesagens da filial solicitada")
    void listarPorFilial_deveRetornarDTOs() {
        when(pesagemRepository.findByFilialId(1L)).thenReturn(List.of(buildPesagem(3L)));

        var result = service.listarPorFilial(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFilial()).isEqualTo("Filial Sul");
        verify(pesagemRepository).findByFilialId(1L);
    }

    @Test
    @DisplayName("listarTodas() deve retornar todas as pesagens")
    void listarTodas_deveRetornarTodasAsPesagens() {
        when(pesagemRepository.findAll()).thenReturn(List.of(buildPesagem(1L), buildPesagem(2L)));

        var result = service.listarTodas();

        assertThat(result).hasSize(2);
        verify(pesagemRepository).findAll();
    }

    @Test
    @DisplayName("listarPorTransacao() deve retornar lista vazia quando não há pesagens")
    void listarPorTransacao_deveRetornarListaVaziaSeNaoHouver() {
        when(pesagemRepository.findByTransacaoId(99L)).thenReturn(List.of());

        var result = service.listarPorTransacao(99L);

        assertThat(result).isEmpty();
    }
}
