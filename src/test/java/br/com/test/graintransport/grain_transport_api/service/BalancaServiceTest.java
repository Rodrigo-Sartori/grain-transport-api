package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.domain.Balanca;
import br.com.test.graintransport.grain_transport_api.domain.Filial;
import br.com.test.graintransport.grain_transport_api.dto.BalancaRequestDTO;
import br.com.test.graintransport.grain_transport_api.repository.BalancaRepository;
import br.com.test.graintransport.grain_transport_api.repository.FilialRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalancaService")
class BalancaServiceTest {

    @Mock BalancaRepository balancaRepository;
    @Mock FilialRepository filialRepository;
    @InjectMocks BalancaService service;

    private static final Filial FILIAL = new Filial(3L, "Filial Norte", "Cascavel");

    @Test
    @DisplayName("listar() deve mapear entidades para DTOs")
    void listar_deveMappearParaDTO() {
        var balanca = new Balanca(1L, "BAL-001", "chave-secreta", FILIAL);
        when(balancaRepository.findAll()).thenReturn(List.of(balanca));

        var result = service.listar();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).codigo()).isEqualTo("BAL-001");
        assertThat(result.get(0).filialId()).isEqualTo(3L);
        assertThat(result.get(0).filialNome()).isEqualTo("Filial Norte");
    }

    @Test
    @DisplayName("salvar() deve resolver Filial e converter DTO → entidade → DTO")
    void salvar_deveResolverFilialERetornarDTO() {
        var dto = new BalancaRequestDTO("BAL-002", "nova-chave", 3L);
        var salva = new Balanca(7L, "BAL-002", "nova-chave", FILIAL);

        when(filialRepository.findById(3L)).thenReturn(Optional.of(FILIAL));
        when(balancaRepository.save(any())).thenReturn(salva);

        var result = service.salvar(dto);

        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.codigo()).isEqualTo("BAL-002");
        assertThat(result.filialId()).isEqualTo(3L);
        verify(filialRepository).findById(3L);
        verify(balancaRepository).save(any(Balanca.class));
    }

    @Test
    @DisplayName("salvar() deve lançar EntityNotFoundException quando filialId não existe")
    void salvar_deveLancarExceptionSeFilialNaoEncontrada() {
        var dto = new BalancaRequestDTO("BAL-003", "chave", 99L);
        when(filialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.salvar(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");

        verify(balancaRepository, never()).save(any());
    }
}
