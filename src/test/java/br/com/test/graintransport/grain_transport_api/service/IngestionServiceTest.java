package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.dto.LeituraBalancaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IngestionService")
class IngestionServiceTest {

    private IngestionService service;

    @BeforeEach
    void setUp() {
        service = new IngestionService();
        ReflectionTestUtils.setField(service, "windowSize", 5);
    }

    private LeituraBalancaDTO leitura(String plate, double weight) {
        return LeituraBalancaDTO.builder()
                .id(1L)
                .plate(plate)
                .weight(weight)
                .build();
    }

    @Nested
    @DisplayName("adicionarLeitura()")
    class AdicionarLeitura {

        @Test
        @DisplayName("deve adicionar leitura no buffer da placa correta")
        void deveAdicionarNaPlacaCorreta() {
            service.adicionarLeitura(leitura("ABC-1234", 10000.0));

            assertThat(service.getBuffer()).containsKey("ABC-1234");
            assertThat(service.getBuffer().get("ABC-1234")).hasSize(1);
        }

        @Test
        @DisplayName("placas diferentes devem ter buffers independentes")
        void placasDiferentesDevemTerBuffersIndependentes() {
            service.adicionarLeitura(leitura("ABC-1234", 10000.0));
            service.adicionarLeitura(leitura("DEF-5678", 12000.0));

            assertThat(service.getBuffer()).hasSize(2);
            assertThat(service.getBuffer().get("ABC-1234")).hasSize(1);
            assertThat(service.getBuffer().get("DEF-5678")).hasSize(1);
        }

        @Test
        @DisplayName("deve manter janela deslizante de no máximo windowSize leituras")
        void deveManterJanelaDeslizante() {
            for (int i = 1; i <= 8; i++) {
                service.adicionarLeitura(leitura("ABC-1234", 10000.0 + i));
            }
            assertThat(service.getBuffer().get("ABC-1234")).hasSize(5);
        }

        @Test
        @DisplayName("janela deslizante deve manter as leituras mais recentes")
        void deveManterLeiturasMaisRecentes() {
            for (int i = 1; i <= 7; i++) {
                service.adicionarLeitura(leitura("ABC-1234", i * 100.0));
            }
            var buffer = service.getBuffer().get("ABC-1234");
            assertThat(buffer.peekFirst().getWeight()).isEqualTo(300.0);
            assertThat(buffer.peekLast().getWeight()).isEqualTo(700.0);
        }
    }

    @Nested
    @DisplayName("limparBuffer()")
    class LimparBuffer {

        @Test
        @DisplayName("deve remover a placa do buffer")
        void deveRemoverPlacaDoBuffer() {
            service.adicionarLeitura(leitura("ABC-1234", 10000.0));
            service.limparBuffer("ABC-1234");

            assertThat(service.getBuffer()).doesNotContainKey("ABC-1234");
        }

        @Test
        @DisplayName("limpar placa inexistente não deve lançar exceção")
        void limparPlacaInexistenteNaoDeveLancarExcecao() {
            assertThat(service.getBuffer()).isEmpty();
            service.limparBuffer("PLACA-INEXISTENTE");
            assertThat(service.getBuffer()).isEmpty();
        }
    }

    @Nested
    @DisplayName("gerarChaveIdempotencia()")
    class GerarChaveIdempotencia {

        @Test
        @DisplayName("momentos diferentes devem gerar chaves diferentes")
        void momentosDiferentesDevemGerarChavesDiferentes() {
            String key1 = service.gerarChaveIdempotencia(1L, "ABC-1234");
            String key2 = service.gerarChaveIdempotencia(2L, "ABC-1234");

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("IDs de balança diferentes devem gerar chaves diferentes")
        void idsDiferentesDevemGerarChavesDiferentes() {
            String key1 = service.gerarChaveIdempotencia(1L, "ABC-1234");
            String key2 = service.gerarChaveIdempotencia(2L, "ABC-1234");

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("placas diferentes devem gerar chaves diferentes")
        void placasDiferentesDevemGerarChavesDiferentes() {
            String key1 = service.gerarChaveIdempotencia(1L, "ABC-1234");
            String key2 = service.gerarChaveIdempotencia(1L, "DEF-5678");

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("chave deve conter o ID da balança e a placa")
        void chaveDeveConterIdEPlaca() {
            String key = service.gerarChaveIdempotencia(1L, "ABC-1234");

            assertThat(key).contains("1").contains("ABC-1234");
        }
    }

    @Nested
    @DisplayName("isDuplicata() / marcarProcessada()")
    class Idempotencia {

        @Test
        @DisplayName("chave não marcada não deve ser duplicata")
        void chaveNaoMarcada_naoEhDuplicata() {
            assertThat(service.isDuplicata("chave-nova")).isFalse();
        }

        @Test
        @DisplayName("após marcarProcessada a chave deve ser detectada como duplicata")
        void aposMarcar_deveSerDuplicata() {
            service.marcarProcessada("chave-X");

            assertThat(service.isDuplicata("chave-X")).isTrue();
        }

        @Test
        @DisplayName("chaves diferentes não devem se interferir")
        void chavesDiferentes_naoSeInterferem() {
            service.marcarProcessada("chave-A");

            assertThat(service.isDuplicata("chave-A")).isTrue();
            assertThat(service.isDuplicata("chave-B")).isFalse();
        }

        @Test
        @DisplayName("marcarProcessada deve remover entradas expiradas além de 2 janelas")
        void marcarProcessada_removeEntradasExpiradas() {
            service.marcarProcessada("chave-velha");

            @SuppressWarnings("unchecked")
            var processedKeys = (java.util.Map<String, Long>)
                    ReflectionTestUtils.getField(service, "processedKeys");
            long expirado = System.currentTimeMillis() - (3 * IngestionService.IDEMPOTENCY_WINDOW_MS);
            processedKeys.put("chave-velha", expirado);

            service.marcarProcessada("chave-nova");

            assertThat(service.isDuplicata("chave-velha")).isFalse(); // removida
            assertThat(service.isDuplicata("chave-nova")).isTrue();   // mantida
        }
    }
}
