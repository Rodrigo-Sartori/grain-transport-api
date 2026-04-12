package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.dto.TransacaoRequestDTO;
import br.com.test.graintransport.grain_transport_api.dto.TransacaoResponseDTO;
import br.com.test.graintransport.grain_transport_api.repository.BalancaRepository;
import br.com.test.graintransport.grain_transport_api.service.TransacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransacaoController.class)
@DisplayName("TransacaoController")
class TransacaoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean TransacaoService transacaoService;
    @MockitoBean BalancaRepository balancaRepository;

    private TransacaoResponseDTO responseAberta() {
        return new TransacaoResponseDTO(1L, 1L, "ABC-1234", "Soja", "Filial Sudoeste", LocalDateTime.now(), null);
    }

    @Test
    @DisplayName("GET /abertas → 200 com lista")
    void listarAbertas_deveRetornarLista() throws Exception {
        when(transacaoService.listarAbertas()).thenReturn(List.of(responseAberta()));

        mockMvc.perform(get("/api/cadastro/transacao/abertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].placa").value("ABC-1234"));
    }

    @Test
    @DisplayName("POST / → 200 abre transação")
    void abrir_comPayloadValido_deveRetornar200() throws Exception {
        when(transacaoService.abrir(any())).thenReturn(responseAberta());

        mockMvc.perform(post("/api/cadastro/transacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TransacaoRequestDTO(1L, 1L, 1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoGrao").value("Soja"));
    }

    @Test
    @DisplayName("POST / → 400 com payload inválido")
    void abrir_comPayloadInvalido_deveRetornar400() throws Exception {
        mockMvc.perform(post("/api/cadastro/transacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /{id}/fechar → 200 com transação fechada")
    void fechar_comIdValido_deveRetornar200() throws Exception {
        var fechada = new TransacaoResponseDTO(1L, 1L, "ABC-1234", "Soja", "Filial Sudoeste", LocalDateTime.now(), LocalDateTime.now());
        when(transacaoService.fechar(eq(1L))).thenReturn(fechada);

        mockMvc.perform(patch("/api/cadastro/transacao/1/fechar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalizadaEm").isNotEmpty());
    }

    @Test
    @DisplayName("PATCH /{id}/fechar → 404 quando não existe")
    void fechar_comIdInexistente_deveRetornar404() throws Exception {
        when(transacaoService.fechar(eq(99L)))
                .thenThrow(new EntityNotFoundException("Transação não encontrada: 99"));

        mockMvc.perform(patch("/api/cadastro/transacao/99/fechar"))
                .andExpect(status().isNotFound());
    }
}
