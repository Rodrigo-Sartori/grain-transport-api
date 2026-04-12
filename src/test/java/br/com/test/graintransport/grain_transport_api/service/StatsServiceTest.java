package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.domain.*;
import br.com.test.graintransport.grain_transport_api.repository.PesagemRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatsService")
class StatsServiceTest {

    @Mock PesagemRepository pesagemRepository;
    @InjectMocks StatsService service;

    private Pesagem pesagemSoja;
    private Pesagem pesagemMilho;

    @BeforeEach
    void setUp() {
        var filialNorte  = new Filial(1L, "Filial Norte", "Cascavel");
        var filialSul    = new Filial(2L, "Filial Sul", "Maringá");
        var caminhaoA    = new Caminhao(1L, "ABC-1234", 8500.0);
        var caminhaoB    = new Caminhao(2L, "XYZ-9999", 9000.0);
        var soja         = new TipoGrao(1L, "Soja", new BigDecimal("2800.00"), 1500.0);
        var milho        = new TipoGrao(2L, "Milho", new BigDecimal("1900.00"), 800.0);

        var transacaoSoja = TransacaoTransporte.builder()
                .id(1L).caminhao(caminhaoA).tipoGrao(soja).filial(filialNorte)
                .iniciadaEm(LocalDateTime.of(2025, 6, 10, 8, 0)).build();

        var transacaoMilho = TransacaoTransporte.builder()
                .id(2L).caminhao(caminhaoB).tipoGrao(milho).filial(filialSul)
                .iniciadaEm(LocalDateTime.of(2025, 6, 15, 10, 0)).build();

        pesagemSoja = Pesagem.builder()
                .id(1L).transacao(transacaoSoja)
                .balanca(new Balanca(1L, "BAL-001", "k1", filialNorte))
                .pesoBruto(18000.0).tara(8500.0).pesoLiquido(9500.0)
                .custoCarga(new BigDecimal("29260.00")).margemAplicada(new BigDecimal("0.1000"))
                .pesadoEm(LocalDateTime.of(2025, 6, 10, 8, 30))
                .build();

        pesagemMilho = Pesagem.builder()
                .id(2L).transacao(transacaoMilho)
                .balanca(new Balanca(2L, "BAL-002", "k2", filialSul))
                .pesoBruto(20000.0).tara(9000.0).pesoLiquido(11000.0)
                .custoCarga(new BigDecimal("23100.00")).margemAplicada(new BigDecimal("0.0500"))
                .pesadoEm(LocalDateTime.of(2025, 6, 15, 10, 30))
                .build();
    }

    @Test
    @DisplayName("pesagens() deve retornar totais corretos")
    void pesagens_totaisCorretos() {
        when(pesagemRepository.findByFiltro(any(), any(), any(), any(), any()))
                .thenReturn(List.of(pesagemSoja, pesagemMilho));

        var result = service.pesagens(null, null, null, null, null, GroupBy.FILIAL);

        assertThat(result.totalPesagens()).isEqualTo(2);
        assertThat(result.pesoBrutoTotalKg()).isEqualTo(38000.0);
        assertThat(result.pesoLiquidoTotalKg()).isEqualTo(20500.0);
    }

    @Test
    @DisplayName("pesagens() agrupado por FILIAL deve gerar 2 grupos ordenados por peso decrescente")
    void pesagens_agrupadoPorFilial() {
        when(pesagemRepository.findByFiltro(any(), any(), any(), any(), any()))
                .thenReturn(List.of(pesagemSoja, pesagemMilho));

        var result = service.pesagens(null, null, null, null, null, GroupBy.FILIAL);

        assertThat(result.agrupamento()).hasSize(2);
        assertThat(result.agrupamento().get(0).label()).isEqualTo("Filial Sul");
        assertThat(result.agrupamento().get(0).valor()).isEqualByComparingTo(new BigDecimal("11000.00"));
    }

    @Test
    @DisplayName("pesagens() agrupado por GRAO deve usar nome do grão como label")
    void pesagens_agrupadoPorGrao() {
        when(pesagemRepository.findByFiltro(any(), any(), any(), any(), any()))
                .thenReturn(List.of(pesagemSoja, pesagemMilho));

        var result = service.pesagens(null, null, null, null, null, GroupBy.GRAO);

        assertThat(result.agrupamento()).hasSize(2);
        assertThat(result.agrupamento().stream().map(g -> g.label()))
                .containsExactlyInAnyOrder("Soja", "Milho");
    }

    @Test
    @DisplayName("pesagens() agrupado por PERIODO deve usar formato yyyy-MM")
    void pesagens_agrupadoPorPeriodo() {
        when(pesagemRepository.findByFiltro(any(), any(), any(), any(), any()))
                .thenReturn(List.of(pesagemSoja, pesagemMilho));

        var result = service.pesagens(null, null, null, null, null, GroupBy.PERIODO);

        assertThat(result.agrupamento()).hasSize(1);
        assertThat(result.agrupamento().get(0).label()).isEqualTo("2025-06");
        assertThat(result.agrupamento().get(0).quantidade()).isEqualTo(2);
    }

    @Test
    @DisplayName("pesagens() deve retornar agrupamento vazio quando não há dados")
    void pesagens_semDados_retornaVazio() {
        when(pesagemRepository.findByFiltro(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        var result = service.pesagens(1L, null, null, null, null, GroupBy.FILIAL);

        assertThat(result.totalPesagens()).isZero();
        assertThat(result.agrupamento()).isEmpty();
    }

    @Test
    @DisplayName("custos() deve retornar custo total e médio corretos")
    void custos_totaisCorretos() {
        when(pesagemRepository.findByFiltro(any(), any(), any(), any(), any()))
                .thenReturn(List.of(pesagemSoja, pesagemMilho));

        var result = service.custos(null, null, null, null, null, GroupBy.CAMINHAO);

        assertThat(result.custoTotalReais()).isEqualByComparingTo(new BigDecimal("52360.00"));
        assertThat(result.custoMedioReais()).isEqualByComparingTo(new BigDecimal("26180.00"));
        assertThat(result.agrupamento()).hasSize(2);
    }

    @Test
    @DisplayName("custos() deve retornar custo médio zero quando lista vazia")
    void custos_semDados_medioZero() {
        when(pesagemRepository.findByFiltro(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        var result = service.custos(null, null, null, null, null, GroupBy.FILIAL);

        assertThat(result.custoTotalReais()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.custoMedioReais()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("lucros() deve calcular lucro estimado = custoCarga × margemAplicada")
    void lucros_calculoCorreto() {
        when(pesagemRepository.findByFiltro(any(), any(), any(), any(), any()))
                .thenReturn(List.of(pesagemSoja, pesagemMilho));

        var result = service.lucros(null, null, null, null, null, GroupBy.GRAO);

        assertThat(result.lucroEstimadoTotalReais()).isEqualByComparingTo(new BigDecimal("4081.00"));
        assertThat(result.margemMediaPercent()).isEqualByComparingTo(new BigDecimal("7.50"));
    }

    @Test
    @DisplayName("lucros() agrupamento deve ordenar por lucro decrescente")
    void lucros_rankingDecrescente() {
        when(pesagemRepository.findByFiltro(any(), any(), any(), any(), any()))
                .thenReturn(List.of(pesagemSoja, pesagemMilho));

        var result = service.lucros(null, null, null, null, null, GroupBy.GRAO);

        assertThat(result.agrupamento().get(0).label()).isEqualTo("Soja");
        assertThat(result.agrupamento().get(1).label()).isEqualTo("Milho");
    }

    @Test
    @DisplayName("lucros() deve converter margem para porcentagem")
    void lucros_margemEmPorcentagem() {
        when(pesagemRepository.findByFiltro(any(), any(), any(), any(), any()))
                .thenReturn(List.of(pesagemSoja)); // só soja: 10%

        var result = service.lucros(null, null, null, null, null, GroupBy.FILIAL);

        assertThat(result.margemMediaPercent()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("lucros() deve repassar filtro de datas corretamente ao repository")
    void lucros_repassaFiltroDeDataAoRepository() {
        var inicio = LocalDate.of(2025, 6, 1);
        var fim    = LocalDate.of(2025, 6, 30);

        when(pesagemRepository.findByFiltro(
                isNull(),
                isNull(),
                isNull(),
                eq(inicio.atStartOfDay()),
                eq(fim.atTime(23, 59, 59))
        )).thenReturn(List.of(pesagemSoja));

        var result = service.lucros(null, null, null, inicio, fim, GroupBy.PERIODO);

        assertThat(result.lucroEstimadoTotalReais()).isEqualByComparingTo(new BigDecimal("2926.00"));
    }
}
