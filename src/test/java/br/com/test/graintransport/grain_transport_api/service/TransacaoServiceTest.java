package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.domain.Caminhao;
import br.com.test.graintransport.grain_transport_api.domain.Filial;
import br.com.test.graintransport.grain_transport_api.domain.TipoGrao;
import br.com.test.graintransport.grain_transport_api.domain.TransacaoTransporte;
import br.com.test.graintransport.grain_transport_api.dto.TransacaoRequestDTO;
import br.com.test.graintransport.grain_transport_api.exception.CaminhaoEmTransporteException;
import br.com.test.graintransport.grain_transport_api.repository.CaminhaoRepository;
import br.com.test.graintransport.grain_transport_api.repository.FilialRepository;
import br.com.test.graintransport.grain_transport_api.repository.TipoGraoRepository;
import br.com.test.graintransport.grain_transport_api.repository.TransacaoTransporteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransacaoService")
class TransacaoServiceTest {

    @Mock
    TransacaoTransporteRepository transacaoRepository;
    @Mock
    CaminhaoRepository caminhaoRepository;
    @Mock
    TipoGraoRepository tipoGraoRepository;
    @Mock
    FilialRepository filialRepository;

    @InjectMocks
    TransacaoService service;

    private Caminhao caminhao() {
        return new Caminhao(1L, "ABC-1234", 8500.0);
    }

    private TipoGrao grao() {
        return new TipoGrao(2L, "Soja", BigDecimal.valueOf(2800.0), 1500.0);
    }

    private Filial filial() {
        return new Filial(3L, "Filial Norte", "Sinop");
    }

    @Nested
    @DisplayName("abrir()")
    class Abrir {

        @Test
        @DisplayName("deve retornar TransacaoResponseDTO com dados corretos quando caminhão está livre")
        void deveRetornarResponseDTO() {
            when(caminhaoRepository.findById(1L)).thenReturn(Optional.of(caminhao()));
            when(tipoGraoRepository.findById(2L)).thenReturn(Optional.of(grao()));
            when(filialRepository.findById(3L)).thenReturn(Optional.of(filial()));
            when(transacaoRepository.findAbertasByCaminhao(1L)).thenReturn(List.of());
            when(transacaoRepository.save(any())).thenAnswer(inv -> {
                TransacaoTransporte t = inv.getArgument(0);
                t.setId(99L);
                return t;
            });

            var result = service.abrir(new TransacaoRequestDTO(1L, 2L, 3L));

            assertThat(result.id()).isEqualTo(99L);
            assertThat(result.placa()).isEqualTo("ABC-1234");
            assertThat(result.tipoGrao()).isEqualTo("Soja");
            assertThat(result.filial()).isEqualTo("Filial Norte");
            assertThat(result.finalizadaEm()).isNull();
        }

        @Test
        @DisplayName("deve lançar CaminhaoEmTransporteException quando caminhão já tem transação aberta")
        void deveLancarExcecaoSeCaminhaoJaEmTransporte() {
            var transacaoAberta = TransacaoTransporte.builder()
                    .id(10L).caminhao(caminhao()).tipoGrao(grao()).filial(filial()).build();

            when(caminhaoRepository.findById(1L)).thenReturn(Optional.of(caminhao()));
            when(tipoGraoRepository.findById(2L)).thenReturn(Optional.of(grao()));
            when(filialRepository.findById(3L)).thenReturn(Optional.of(filial()));
            when(transacaoRepository.findAbertasByCaminhao(1L)).thenReturn(List.of(transacaoAberta));

            assertThatThrownBy(() -> service.abrir(new TransacaoRequestDTO(1L, 2L, 3L)))
                    .isInstanceOf(CaminhaoEmTransporteException.class)
                    .hasMessageContaining("ABC-1234");
        }

        @Test
        @DisplayName("não deve salvar transação quando caminhão está em viagem")
        void naoDeveSalvarQuandoCaminhaoEmViagem() {
            var transacaoAberta = TransacaoTransporte.builder()
                    .id(10L).caminhao(caminhao()).tipoGrao(grao()).filial(filial()).build();

            when(caminhaoRepository.findById(1L)).thenReturn(Optional.of(caminhao()));
            when(tipoGraoRepository.findById(2L)).thenReturn(Optional.of(grao()));
            when(filialRepository.findById(3L)).thenReturn(Optional.of(filial()));
            when(transacaoRepository.findAbertasByCaminhao(1L)).thenReturn(List.of(transacaoAberta));

            assertThatThrownBy(() -> service.abrir(new TransacaoRequestDTO(1L, 2L, 3L)))
                    .isInstanceOf(CaminhaoEmTransporteException.class);

            org.mockito.Mockito.verify(transacaoRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException para caminhão inexistente")
        void deveLancarExcecaoSeCaminhaoNaoExiste() {
            when(caminhaoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.abrir(new TransacaoRequestDTO(99L, 1L, 1L)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException para grão inexistente")
        void deveLancarExcecaoSeGraoNaoExiste() {
            when(caminhaoRepository.findById(1L)).thenReturn(Optional.of(caminhao()));
            when(tipoGraoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.abrir(new TransacaoRequestDTO(1L, 99L, 1L)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("fechar()")
    class Fechar {

        @Test
        @DisplayName("deve retornar TransacaoResponseDTO com finalizadaEm preenchido")
        void deveFecharERetornarDTO() {
            var transacao = TransacaoTransporte.builder()
                    .id(1L).caminhao(caminhao()).tipoGrao(grao()).filial(filial()).build();

            when(transacaoRepository.findById(1L)).thenReturn(Optional.of(transacao));
            when(transacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var result = service.fechar(1L);

            assertThat(result.finalizadaEm()).isNotNull();
            assertThat(result.placa()).isEqualTo("ABC-1234");
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException para transação inexistente")
        void deveLancarExcecaoSeNaoExiste() {
            when(transacaoRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.fechar(404L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("404");
        }
    }
}
