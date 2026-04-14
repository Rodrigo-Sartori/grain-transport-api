package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.dto.LeituraBalancaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    static final long IDEMPOTENCY_WINDOW_MS = 30_000L;

    @Value("${stabilization.window-size:5}")
    private int windowSize;

    private final Map<String, Deque<LeituraBalancaDTO>> buffer = new ConcurrentHashMap<>();

    private final Map<String, Long> processedKeys = new ConcurrentHashMap<>();

    public void adicionarLeitura(LeituraBalancaDTO leitura) {
        buffer.compute(leitura.getPlate(), (plate, deque) -> {
            if (deque == null) deque = new ArrayDeque<>();
            deque.addLast(leitura);
            while (deque.size() > windowSize) deque.pollFirst();
            return deque;
        });
        log.info("Leitura recebida: balanca={}, plate={}, weight={}kg, buffer={}",
                leitura.getId(), leitura.getPlate(), leitura.getWeight(),
                buffer.get(leitura.getPlate()).size());
    }

    public Map<String, Deque<LeituraBalancaDTO>> getBuffer() {
        return buffer;
    }

    public void limparBuffer(String plate) {
        buffer.remove(plate);
        log.info("Buffer limpo: plate={}", plate);
    }

    public String gerarChaveIdempotencia(Long balancaId, String plate) {
        var chave = balancaId + "_" + plate + "_" + LocalDateTime.now();
        System.out.println(chave);
        return chave;
    }

    public boolean isDuplicata(String chave) {
        return processedKeys.containsKey(chave);
    }

    public void marcarProcessada(String chave) {
        processedKeys.put(chave, System.currentTimeMillis());
        long limiteMs = System.currentTimeMillis() - (2 * IDEMPOTENCY_WINDOW_MS);
        processedKeys.entrySet().removeIf(e -> e.getValue() < limiteMs);
        log.debug("Chave idempotente marcada: {}", chave);
    }
}
