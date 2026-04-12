package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.dto.CaminhaoResponseDTO;
import br.com.test.graintransport.grain_transport_api.repository.BalancaRepository;
import br.com.test.graintransport.grain_transport_api.service.CaminhaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CaminhaoController.class)
@DisplayName("CaminhaoController")
class CaminhaoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean CaminhaoService caminhaoService;
    @MockitoBean BalancaRepository balancaRepository;

    @Test
    @DisplayName("GET /api/cadastro/caminhao → 200 com lista de caminhões")
    void listar_deveRetornar200ComLista() throws Exception {
        when(caminhaoService.listar()).thenReturn(List.of(
                new CaminhaoResponseDTO(1L, "ABC-1234", 8500.0)
        ));

        mvc.perform(get("/api/cadastro/caminhao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].placa").value("ABC-1234"))
                .andExpect(jsonPath("$[0].tara").value(8500.0));
    }

    @Test
    @DisplayName("POST /api/cadastro/caminhao → 200 com caminhão criado")
    void salvar_deveRetornar200ComDTO() throws Exception {
        when(caminhaoService.salvar(any())).thenReturn(
                new CaminhaoResponseDTO(10L, "XYZ-9999", 9000.0)
        );

        String body = """
                {"placa": "XYZ-9999", "tara": 9000.0}
                """;

        mvc.perform(post("/api/cadastro/caminhao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.placa").value("XYZ-9999"))
                .andExpect(jsonPath("$.tara").value(9000.0));
    }

    @Test
    @DisplayName("POST /api/cadastro/caminhao → 400 quando placa ausente")
    void salvar_deveRetornar400SePlacaAusente() throws Exception {
        mvc.perform(post("/api/cadastro/caminhao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tara\": 9000.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/cadastro/caminhao → 400 quando tara ausente")
    void salvar_deveRetornar400SeTaraAusente() throws Exception {
        mvc.perform(post("/api/cadastro/caminhao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"placa\": \"XYZ-9999\"}"))
                .andExpect(status().isBadRequest());
    }
}
