package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.domain.Caminhao;
import br.com.test.graintransport.grain_transport_api.dto.CaminhaoRequestDTO;
import br.com.test.graintransport.grain_transport_api.repository.CaminhaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CaminhaoService")
class CaminhaoServiceTest {

    @Mock CaminhaoRepository caminhaoRepository;
    @InjectMocks CaminhaoService service;

    @Test
    @DisplayName("listar() deve mapear entidades para DTOs")
    void listar_deveMappearParaDTO() {
        when(caminhaoRepository.findAll()).thenReturn(List.of(new Caminhao(1L, "ABC-1234", 8500.0)));

        var result = service.listar();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).placa()).isEqualTo("ABC-1234");
        assertThat(result.get(0).tara()).isEqualTo(8500.0);
    }

    @Test
    @DisplayName("salvar() deve converter DTO → entidade → DTO")
    void salvar_deveConverterERetornarDTO() {
        var dto = new CaminhaoRequestDTO("XYZ-9999", 9000.0);
        when(caminhaoRepository.save(any())).thenReturn(new Caminhao(10L, "XYZ-9999", 9000.0));

        var result = service.salvar(dto);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.placa()).isEqualTo("XYZ-9999");
        verify(caminhaoRepository).save(any(Caminhao.class));
    }
}
