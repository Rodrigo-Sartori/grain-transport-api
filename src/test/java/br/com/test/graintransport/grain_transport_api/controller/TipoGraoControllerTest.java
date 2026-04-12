package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.dto.TipoGraoResponseDTO;
import br.com.test.graintransport.grain_transport_api.repository.BalancaRepository;
import br.com.test.graintransport.grain_transport_api.service.TipoGraoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TipoGraoController.class)
@DisplayName("TipoGraoController")
class TipoGraoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean TipoGraoService tipoGraoService;
    @MockitoBean BalancaRepository balancaRepository;

    @Test
    @DisplayName("GET /api/cadastro/grao → 200 com lista de grãos")
    void listar_deveRetornar200ComLista() throws Exception {
        when(tipoGraoService.listar()).thenReturn(List.of(
                new TipoGraoResponseDTO(1L, "Soja", new BigDecimal("2800.00"), 1500.0)
        ));

        mvc.perform(get("/api/cadastro/grao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nome").value("Soja"))
                .andExpect(jsonPath("$[0].precoPorTonelada").value(2800.00))
                .andExpect(jsonPath("$[0].estoqueToneladas").value(1500.0));
    }

    @Test
    @DisplayName("POST /api/cadastro/grao → 200 com grão criado")
    void salvar_deveRetornar200ComDTO() throws Exception {
        when(tipoGraoService.salvar(any())).thenReturn(
                new TipoGraoResponseDTO(5L, "Milho", new BigDecimal("1900.00"), 800.0)
        );

        String body = """
                {"nome": "Milho", "precoPorTonelada": 1900.00, "estoqueToneladas": 800.0}
                """;

        mvc.perform(post("/api/cadastro/grao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.nome").value("Milho"))
                .andExpect(jsonPath("$.precoPorTonelada").value(1900.00))
                .andExpect(jsonPath("$.estoqueToneladas").value(800.0));
    }

    @Test
    @DisplayName("POST /api/cadastro/grao → 400 quando nome ausente")
    void salvar_deveRetornar400SeNomeAusente() throws Exception {
        mvc.perform(post("/api/cadastro/grao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"precoPorTonelada\": 1900.00, \"estoqueToneladas\": 800.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/cadastro/grao → 400 quando precoPorTonelada ausente")
    void salvar_deveRetornar400SePrecoAusente() throws Exception {
        mvc.perform(post("/api/cadastro/grao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Milho\", \"estoqueToneladas\": 800.0}"))
                .andExpect(status().isBadRequest());
    }
}
