package br.com.test.graintransport.grain_transport_api.controller;

import br.com.test.graintransport.grain_transport_api.domain.Balanca;
import br.com.test.graintransport.grain_transport_api.dto.LeituraBalancaDTO;
import br.com.test.graintransport.grain_transport_api.repository.BalancaRepository;
import br.com.test.graintransport.grain_transport_api.service.IngestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IngestionController.class)
@DisplayName("IngestionController")
class IngestionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean IngestionService ingestionService;
    @MockitoBean BalancaRepository balancaRepository;

    private LeituraBalancaDTO payload() {
        return LeituraBalancaDTO.builder()
                .id(1L)
                .plate("ABC-1234")
                .weight(15420.5)
                .build();
    }

    @Test
    @DisplayName("POST /api/ingest → 202 com X-Api-Key válida e leitura nova")
    void ingerir_comApiKeyValida_deveRetornar202() throws Exception {
        var balanca = new Balanca(1L, "BAL-001", "key-balanca-001-dev", null);
        when(balancaRepository.findByApiKey("key-balanca-001-dev")).thenReturn(Optional.of(balanca));
        when(ingestionService.gerarChaveIdempotencia(1L, "ABC-1234")).thenReturn("1_ABC-1234_999");
        when(ingestionService.isDuplicata("1_ABC-1234_999")).thenReturn(false);

        mockMvc.perform(post("/api/ingest")
                        .header("X-Api-Key", "key-balanca-001-dev")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("accepted"))
                .andExpect(jsonPath("$.plate").value("ABC-1234"))
                .andExpect(jsonPath("$.balanca").value("1"));

        verify(ingestionService).adicionarLeitura(any(LeituraBalancaDTO.class));
        verify(ingestionService).marcarProcessada("1_ABC-1234_999");
    }

    @Test
    @DisplayName("POST /api/ingest → 200 com requisição duplicata na mesma janela de tempo")
    void ingerir_duplicata_deveRetornar200SemReprocessar() throws Exception {
        var balanca = new Balanca(1L, "BAL-001", "key-balanca-001-dev", null);
        when(balancaRepository.findByApiKey("key-balanca-001-dev")).thenReturn(Optional.of(balanca));
        when(ingestionService.gerarChaveIdempotencia(1L, "ABC-1234")).thenReturn("1_ABC-1234_999");
        when(ingestionService.isDuplicata("1_ABC-1234_999")).thenReturn(true);

        mockMvc.perform(post("/api/ingest")
                        .header("X-Api-Key", "key-balanca-001-dev")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("duplicata"))
                .andExpect(jsonPath("$.plate").value("ABC-1234"));

        verify(ingestionService, never()).adicionarLeitura(any());
        verify(ingestionService, never()).marcarProcessada(anyString());
    }

    @Test
    @DisplayName("POST /api/ingest → 401 sem X-Api-Key")
    void ingerir_semApiKey_deveRetornar401() throws Exception {
        mockMvc.perform(post("/api/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload())))
                .andExpect(status().isUnauthorized());

        verify(ingestionService, never()).adicionarLeitura(any());
    }

    @Test
    @DisplayName("POST /api/ingest → 403 com X-Api-Key inválida")
    void ingerir_comApiKeyInvalida_deveRetornar403() throws Exception {
        when(balancaRepository.findByApiKey("chave-errada")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/ingest")
                        .header("X-Api-Key", "chave-errada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload())))
                .andExpect(status().isForbidden());

        verify(ingestionService, never()).adicionarLeitura(any());
    }

    @Test
    @DisplayName("POST /api/ingest → 400 com payload inválido (weight nulo)")
    void ingerir_comPayloadInvalido_deveRetornar400() throws Exception {
        var balanca = new Balanca(1L, "BAL-001", "key-balanca-001-dev", null);
        when(balancaRepository.findByApiKey("key-balanca-001-dev")).thenReturn(Optional.of(balanca));

        mockMvc.perform(post("/api/ingest")
                        .header("X-Api-Key", "key-balanca-001-dev")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"plate\":\"ABC-1234\"}"))
                .andExpect(status().isBadRequest());

        verify(ingestionService, never()).adicionarLeitura(any());
    }
}
