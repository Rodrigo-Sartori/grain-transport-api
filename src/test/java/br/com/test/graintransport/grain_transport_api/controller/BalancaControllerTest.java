package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.dto.BalancaResponseDTO;
import br.com.test.graintransport.grain_transport_api.repository.BalancaRepository;
import br.com.test.graintransport.grain_transport_api.service.BalancaService;
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

@WebMvcTest(BalancaController.class)
@DisplayName("BalancaController")
class BalancaControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean BalancaService balancaService;
    @MockitoBean BalancaRepository balancaRepository; // requerido pelo ApiKeyFilter

    @Test
    @DisplayName("GET /api/cadastro/balanca → 200 com lista de balanças")
    void listar_deveRetornar200ComLista() throws Exception {
        when(balancaService.listar()).thenReturn(List.of(
                new BalancaResponseDTO(1L, "BAL-001", 3L, "Filial Norte")
        ));

        mvc.perform(get("/api/cadastro/balanca"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].codigo").value("BAL-001"))
                .andExpect(jsonPath("$[0].filialId").value(3L))
                .andExpect(jsonPath("$[0].filialNome").value("Filial Norte"));
    }

    @Test
    @DisplayName("POST /api/cadastro/balanca → 200 com balança criada")
    void salvar_deveRetornar200ComDTO() throws Exception {
        when(balancaService.salvar(any())).thenReturn(
                new BalancaResponseDTO(7L, "BAL-002", 3L, "Filial Sul")
        );

        String body = """
                {"codigo": "BAL-002", "apiKey": "chave-secreta", "filialId": 3}
                """;

        mvc.perform(post("/api/cadastro/balanca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.codigo").value("BAL-002"))
                .andExpect(jsonPath("$.filialId").value(3L))
                .andExpect(jsonPath("$.filialNome").value("Filial Sul"));
    }

    @Test
    @DisplayName("POST /api/cadastro/balanca → 400 quando codigo ausente")
    void salvar_deveRetornar400SeCodigoAusente() throws Exception {
        mvc.perform(post("/api/cadastro/balanca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"apiKey\": \"chave-secreta\", \"filialId\": 3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/cadastro/balanca → 400 quando filialId ausente")
    void salvar_deveRetornar400SeFilialIdAusente() throws Exception {
        mvc.perform(post("/api/cadastro/balanca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"BAL-002\", \"apiKey\": \"chave-secreta\"}"))
                .andExpect(status().isBadRequest());
    }
}
