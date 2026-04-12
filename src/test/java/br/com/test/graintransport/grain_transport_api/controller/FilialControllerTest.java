package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.dto.FilialRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.FilialResponseDTO;
import br.com.test.graintransport.grain_transport_api.repository.BalancaRepository;
import br.com.test.graintransport.grain_transport_api.service.FilialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilialController.class)
@DisplayName("FilialController")
class FilialControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean FilialService filialService;
    @MockitoBean BalancaRepository balancaRepository;

    @Test
    @DisplayName("GET → 200 com lista de FilialResponseDTO")
    void listar_deveRetornarLista() throws Exception {
        when(filialService.listar()).thenReturn(List.of(
                new FilialResponseDTO(1L, "Filial Sudoeste", "Cascavel"),
                new FilialResponseDTO(2L, "Filial Norte", "Sinop")
        ));

        mockMvc.perform(get("/api/cadastro/filial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Filial Sudoeste"));
    }

    @Test
    @DisplayName("POST → 200 com filial criada")
    void criar_deveRetornarFilialSalva() throws Exception {
        var request = new FilialRequestDTO("Filial Centro-Oeste", "Rondonópolis");
        when(filialService.salvar(any())).thenReturn(new FilialResponseDTO(3L, "Filial Centro-Oeste", "Rondonópolis"));

        mockMvc.perform(post("/api/cadastro/filial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nome").value("Filial Centro-Oeste"));
    }

    @Test
    @DisplayName("POST → 400 com campos obrigatórios ausentes")
    void criar_semNome_deveRetornar400() throws Exception {
        mockMvc.perform(post("/api/cadastro/filial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cidade\":\"Cascavel\"}"))
                .andExpect(status().isBadRequest());
    }
}
