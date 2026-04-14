package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.domain.Balanca;
import br.com.test.graintransport.grain_transport_api.domain.Caminhao;
import br.com.test.graintransport.grain_transport_api.domain.Filial;
import br.com.test.graintransport.grain_transport_api.domain.Pesagem;
import br.com.test.graintransport.grain_transport_api.domain.TipoGrao;
import br.com.test.graintransport.grain_transport_api.domain.TransacaoTransporte;
import br.com.test.graintransport.grain_transport_api.dto.LeituraBalancaDTO;
import br.com.test.graintransport.grain_transport_api.repository.BalancaRepository;
import br.com.test.graintransport.grain_transport_api.repository.PesagemRepository;
import br.com.test.graintransport.grain_transport_api.repository.TransacaoTransporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StabilizationService")
class StabilizationServiceTest {

    @Mock
    private IngestionService ingestionService;
    @Mock
    private PesagemRepository pesagemRepository;
    @Mock
    private BalancaRepository balancaRepository;
    @Mock
    private TransacaoTransporteRepository transacaoRepository;

    private StabilizationService service;

    @BeforeEach
    void setUp() {
        service = new StabilizationService(ingestionService, pesagemRepository, balancaRepository, transacaoRepository);
        ReflectionTestUtils.setField(service, "windowSize", 5);
        ReflectionTestUtils.setField(service, "varianceThreshold", 3.0);
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private Filial filial() {
        return new Filial(1L, "Filial Norte", "Sinop");
    }

    private Caminhao caminhao(double tara) {
        return new Caminhao(1L, "ABC-1234", tara);
    }

    private TipoGrao grao() {
        return new TipoGrao(1L, "Soja", BigDecimal.valueOf(2800.0), 500.0);
    }

    private Balanca balanca() {
        return new Balanca(1L, "BAL-001", "api-key-test", filial());
    }

    private TransacaoTransporte transacao(double tara) {
        return TransacaoTransporte.builder()
                .id(1L)
                .caminhao(caminhao(tara))
                .tipoGrao(grao())
                .filial(filial())
                .build();
    }

    private Map<String, Deque<LeituraBalancaDTO>> bufferEstavel(double pesoKg) {
        Deque<LeituraBalancaDTO> leituras = new ArrayDeque<>();
        for (int i = 0; i < 5; i++) {
            leituras.add(LeituraBalancaDTO.builder()
                    .id(1L).plate("ABC-1234").weight(pesoKg).build());
        }
        Map<String, Deque<LeituraBalancaDTO>> buffer = new ConcurrentHashMap<>();
        buffer.put("ABC-1234", leituras);
        return buffer;
    }

    @Nested
    @DisplayName("isEstabilizado()")
    class IsEstabilizado {

        @Test
        @DisplayName("deve retornar true quando variação exatamente no threshold")
        void variacaoIgualThreshold_deveEstabilizar() {
            List<Double> pesos = List.of(100.0, 101.0, 102.0, 100.5, 103.0); // variação = 3.0
            assertThat(service.isEstabilizado(pesos)).isTrue();
        }

        @Test
        @DisplayName("deve retornar true quando todos os pesos são iguais")
        void pesosIdenticos_deveEstabilizar() {
            assertThat(service.isEstabilizado(List.of(500.0, 500.0, 500.0, 500.0, 500.0))).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando variação acima do threshold")
        void variacaoAcimaThreshold_naoDeveEstabilizar() {
            List<Double> pesos = List.of(100.0, 104.0, 102.0, 100.5, 100.0); // variação = 4.0
            assertThat(service.isEstabilizado(pesos)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false quando janela menor que windowSize")
        void janelaInsuficiente_naoDeveEstabilizar() {
            assertThat(service.isEstabilizado(List.of(100.0, 100.5, 101.0))).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para lista nula")
        void listaNula_naoDeveEstabilizar() {
            assertThat(service.isEstabilizado(null)).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para lista vazia")
        void listaVazia_naoDeveEstabilizar() {
            assertThat(service.isEstabilizado(List.of())).isFalse();
        }
    }

    @Nested
    @DisplayName("calcularMargem()")
    class CalcularMargem {

        @Test
        @DisplayName("estoque acima de 1000t → margem mínima 5%")
        void estoqueAlto_deveRetornarMargemMinima() {
            assertThat(service.calcularMargem(1500.0)).isEqualTo(0.05);
        }

        @Test
        @DisplayName("estoque exatamente 1000t → 5%")
        void estoqueExatamente1000_deveRetornar5Porcento() {
            assertThat(service.calcularMargem(1000.0)).isEqualTo(0.05);
        }

        @Test
        @DisplayName("estoque abaixo de 100t → margem máxima 20%")
        void estoqueBaixo_deveRetornarMargemMaxima() {
            assertThat(service.calcularMargem(50.0)).isEqualTo(0.20);
        }

        @Test
        @DisplayName("estoque exatamente 100t → 20%")
        void estoqueExatamente100_deveRetornar20Porcento() {
            assertThat(service.calcularMargem(100.0)).isEqualTo(0.20);
        }

        @Test
        @DisplayName("estoque 550t (meio da escala) → ~12.5%")
        void estoqueMedio_deveInterporlar() {
            assertThat(service.calcularMargem(550.0)).isCloseTo(0.125, within(0.001));
        }

        @ParameterizedTest(name = "estoque={0}t → margem={1}")
        @CsvSource({
                "100.0,  0.20",
                "325.0,  0.1625",
                "550.0,  0.125",
                "775.0,  0.0875",
                "1000.0, 0.05"
        })
        @DisplayName("interpolação linear deve ser monotonicamente decrescente")
        void interpolacaoLinear(double estoque, double esperado) {
            assertThat(service.calcularMargem(estoque)).isCloseTo(esperado, within(0.0001));
        }

        @Test
        @DisplayName("margem deve sempre estar entre 5% e 20% independente do estoque")
        void margemSempreNoRange() {
            double[] estoques = {0, 50, 100, 250, 500, 750, 1000, 2000, 9999};
            for (double estoque : estoques) {
                double margem = service.calcularMargem(estoque);
                assertThat(margem)
                        .as("estoque=%s", estoque)
                        .isGreaterThanOrEqualTo(0.05)
                        .isLessThanOrEqualTo(0.20);
            }
        }
    }

    @Nested
    @DisplayName("processarBuffer()")
    class ProcessarBuffer {

        @Test
        @DisplayName("não deve persistir pesagem quando peso bruto é menor que a tara do caminhão")
        void naoDevePersistirQuandoPesoBrutoMenorQueTara() {
            double tara = 8500.0;
            double pesoBrutoAbaixoDaTara = 7000.0; // claramente menor que a tara

            when(ingestionService.getBuffer()).thenReturn(bufferEstavel(pesoBrutoAbaixoDaTara));
            when(balancaRepository.findById(1L)).thenReturn(Optional.of(balanca()));
            when(transacaoRepository.findAbertaByPlaca("ABC-1234")).thenReturn(Optional.of(transacao(tara)));

            service.processarBuffer();

            verify(pesagemRepository, never()).save(any(Pesagem.class));
        }

        @Test
        @DisplayName("não deve persistir pesagem quando peso bruto é igual à tara do caminhão")
        void naoDevePersistirQuandoPesoBrutoIgualATara() {
            double tara = 8500.0;

            when(ingestionService.getBuffer()).thenReturn(bufferEstavel(tara)); // igual à tara
            when(balancaRepository.findById(1L)).thenReturn(Optional.of(balanca()));
            when(transacaoRepository.findAbertaByPlaca("ABC-1234")).thenReturn(Optional.of(transacao(tara)));

            service.processarBuffer();

            verify(pesagemRepository, never()).save(any(Pesagem.class));
        }

        @Test
        @DisplayName("deve limpar o buffer mesmo quando peso bruto é inválido (menor ou igual à tara)")
        void deveLimparBufferMesmoComPesoInvalido() {
            double tara = 8500.0;
            double pesoBrutoInvalido = 7000.0;

            when(ingestionService.getBuffer()).thenReturn(bufferEstavel(pesoBrutoInvalido));
            when(balancaRepository.findById(1L)).thenReturn(Optional.of(balanca()));
            when(transacaoRepository.findAbertaByPlaca("ABC-1234")).thenReturn(Optional.of(transacao(tara)));

            service.processarBuffer();

            verify(ingestionService).limparBuffer("ABC-1234");
        }

        @Test
        @DisplayName("deve persistir pesagem quando peso bruto é maior que a tara")
        void devePersistirQuandoPesoBrutoMaiorQueTara() {
            double tara = 8500.0;
            double pesoBrutoValido = 20000.0; // caminhão carregado

            when(ingestionService.getBuffer()).thenReturn(bufferEstavel(pesoBrutoValido));
            when(balancaRepository.findById(1L)).thenReturn(Optional.of(balanca()));
            when(transacaoRepository.findAbertaByPlaca("ABC-1234")).thenReturn(Optional.of(transacao(tara)));
            when(pesagemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.processarBuffer();

            verify(pesagemRepository).save(any(Pesagem.class));
            verify(ingestionService).limparBuffer("ABC-1234");
        }

        @Test
        @DisplayName("não deve processar quando o buffer está vazio")
        void naoDeveProcessarBufferVazio() {
            when(ingestionService.getBuffer()).thenReturn(new ConcurrentHashMap<>());

            service.processarBuffer();

            verify(pesagemRepository, never()).save(any());
            verify(ingestionService, never()).limparBuffer(any());
        }

        @Test
        @DisplayName("não deve processar quando a janela de leituras é insuficiente")
        void naoDeveProcessarComJanelaInsuficiente() {
            Deque<LeituraBalancaDTO> poucastLeituras = new ArrayDeque<>();
            poucastLeituras.add(LeituraBalancaDTO.builder().id(1L).plate("ABC-1234").weight(20000.0).build());
            poucastLeituras.add(LeituraBalancaDTO.builder().id(1L).plate("ABC-1234").weight(20000.0).build());

            Map<String, Deque<LeituraBalancaDTO>> buffer = new ConcurrentHashMap<>();
            buffer.put("ABC-1234", poucastLeituras);

            when(ingestionService.getBuffer()).thenReturn(buffer);

            service.processarBuffer();

            verify(pesagemRepository, never()).save(any());
        }
    }
}
