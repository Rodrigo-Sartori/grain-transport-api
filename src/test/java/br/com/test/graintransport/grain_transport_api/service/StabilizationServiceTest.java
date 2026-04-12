package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.repository.BalancaRepository;
import br.com.test.graintransport.grain_transport_api.repository.PesagemRepository;
import br.com.test.graintransport.grain_transport_api.repository.TransacaoTransporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;

@DisplayName("StabilizationService")
class StabilizationServiceTest {

    private StabilizationService service;

    @BeforeEach
    void setUp() {
        service = new StabilizationService(
                mock(IngestionService.class),
                mock(PesagemRepository.class),
                mock(BalancaRepository.class),
                mock(TransacaoTransporteRepository.class)
        );
        ReflectionTestUtils.setField(service, "windowSize", 5);
        ReflectionTestUtils.setField(service, "varianceThreshold", 3.0);
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
}
