package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.domain.Filial;
import br.com.test.graintransport.grain_transport_api.dto.FilialRequestDTO;
import br.com.test.graintransport.grain_transport_api.repository.FilialRepository;
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
@DisplayName("FilialService")
class FilialServiceTest {

    @Mock FilialRepository filialRepository;
    @InjectMocks FilialService service;

    @Test
    @DisplayName("listar() deve mapear entidades para DTOs")
    void listar_deveMappearParaDTO() {
        when(filialRepository.findAll()).thenReturn(List.of(new Filial(1L, "Filial A", "Cascavel")));

        var result = service.listar();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).nome()).isEqualTo("Filial A");
        assertThat(result.get(0).cidade()).isEqualTo("Cascavel");
    }

    @Test
    @DisplayName("salvar() deve converter DTO → entidade → DTO")
    void salvar_deveConverterERetornarDTO() {
        var dto = new FilialRequestDTO("Nova Filial", "Cuiabá");
        when(filialRepository.save(any())).thenReturn(new Filial(5L, "Nova Filial", "Cuiabá"));

        var result = service.salvar(dto);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.nome()).isEqualTo("Nova Filial");
        verify(filialRepository).save(any(Filial.class));
    }
}
