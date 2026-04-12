package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.domain.TipoGrao;
import br.com.test.graintransport.grain_transport_api.dto.TipoGraoRequestDTO;
import br.com.test.graintransport.grain_transport_api.repository.TipoGraoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TipoGraoService")
class TipoGraoServiceTest {

    @Mock TipoGraoRepository tipoGraoRepository;
    @InjectMocks TipoGraoService service;

    @Test
    @DisplayName("listar() deve mapear entidades para DTOs")
    void listar_deveMappearParaDTO() {
        when(tipoGraoRepository.findAll()).thenReturn(List.of(
                new TipoGrao(1L, "Soja", new BigDecimal("2800.00"), 1500.0)
        ));

        var result = service.listar();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).nome()).isEqualTo("Soja");
        assertThat(result.get(0).precoPorTonelada()).isEqualByComparingTo(new BigDecimal("2800.00"));
        assertThat(result.get(0).estoqueToneladas()).isEqualTo(1500.0);
    }

    @Test
    @DisplayName("salvar() deve converter DTO → entidade → DTO")
    void salvar_deveConverterERetornarDTO() {
        var dto = new TipoGraoRequestDTO("Milho", new BigDecimal("1900.00"), 800.0);
        when(tipoGraoRepository.save(any())).thenReturn(
                new TipoGrao(5L, "Milho", new BigDecimal("1900.00"), 800.0)
        );

        var result = service.salvar(dto);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.nome()).isEqualTo("Milho");
        assertThat(result.precoPorTonelada()).isEqualByComparingTo(new BigDecimal("1900.00"));
        verify(tipoGraoRepository).save(any(TipoGrao.class));
    }
}
