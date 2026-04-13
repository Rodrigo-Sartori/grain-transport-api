package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.domain.Balanca;
import br.com.test.graintransport.grain_transport_api.domain.Pesagem;
import br.com.test.graintransport.grain_transport_api.domain.TransacaoTransporte;
import br.com.test.graintransport.grain_transport_api.dto.LeituraBalancaDTO;
import br.com.test.graintransport.grain_transport_api.repository.BalancaRepository;
import br.com.test.graintransport.grain_transport_api.repository.PesagemRepository;
import br.com.test.graintransport.grain_transport_api.repository.TransacaoTransporteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StabilizationService {

    private static final double MARGEM_MINIMA = 0.05;
    private static final double MARGEM_MAXIMA = 0.20;
    private static final double ESTOQUE_ALTO = 1000.0;
    private static final double ESTOQUE_BAIXO = 100.0;

    @Value("${stabilization.window-size:5}")
    private int windowSize;

    @Value("${stabilization.variance-threshold:3.0}")
    private double varianceThreshold;

    private final IngestionService ingestionService;
    private final PesagemRepository pesagemRepository;
    private final BalancaRepository balancaRepository;
    private final TransacaoTransporteRepository transacaoRepository;

    @Scheduled(fixedDelayString = "${stabilization.scheduler-ms:2000}")
    @Transactional
    public void processarBuffer() {
        Map<String, Deque<LeituraBalancaDTO>> buffer = ingestionService.getBuffer();
        if (buffer.isEmpty()) return;

        buffer.forEach((plate, leituras) -> {
            if (leituras.size() < windowSize) {
                log.debug("Janela insuficiente: plate={} ({}/{})", plate, leituras.size(), windowSize);
                return;
            }

            List<Double> pesos = leituras.stream().map(LeituraBalancaDTO::getWeight).toList();

            if (isEstabilizado(pesos)) {
                LeituraBalancaDTO ultima = leituras.peekLast();
                if (ultima == null) return;

                persistirPesagem(ultima, pesos);
                ingestionService.limparBuffer(plate);
            } else {
                double variacao = pesos.stream().mapToDouble(Double::doubleValue).max().orElse(0)
                        - pesos.stream().mapToDouble(Double::doubleValue).min().orElse(0);
                log.warn("Peso instável: plate={}, variação={}kg (threshold={}kg)", plate, variacao, varianceThreshold);
            }
        });
    }

    private void persistirPesagem(LeituraBalancaDTO leitura, List<Double> pesos) {
        log.info("Janela suficiente: plate={} ({}/{})", leitura.getPlate(), pesos, LocalDateTime.now());

        Balanca balanca = balancaRepository.findById(leitura.getId())
                .orElseThrow(() -> new IllegalArgumentException("Balança não encontrada: " + leitura.getId()));

        TransacaoTransporte transacao = transacaoRepository.findAbertaByPlaca(leitura.getPlate())
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhuma transação aberta para a placa: " + leitura.getPlate()));

        double pesoBruto = pesos.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double tara = transacao.getCaminhao().getTara();
        double pesoLiquido = pesoBruto - tara;
        double estoque = transacao.getTipoGrao().getEstoqueToneladas();
        double margem = calcularMargem(estoque);

        BigDecimal precoPorKg = transacao.getTipoGrao().getPrecoPorTonelada()
                .divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP);
        BigDecimal custoCarga = BigDecimal.valueOf(pesoLiquido)
                .multiply(precoPorKg)
                .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(margem)))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal margemBD = BigDecimal.valueOf(margem).setScale(4, RoundingMode.HALF_UP);

        Pesagem pesagem = Pesagem.builder()
                .transacao(transacao)
                .balanca(balanca)
                .pesoBruto(pesoBruto)
                .tara(tara)
                .pesoLiquido(pesoLiquido)
                .custoCarga(custoCarga)
                .margemAplicada(margemBD)
                .pesadoEm(LocalDateTime.now())
                .build();

        pesagemRepository.save(pesagem);
        log.info("Pesagem salva: plate={}, pesoLíquido={}kg, custo=R${}, margem={}%",
                leitura.getPlate(), pesoLiquido, custoCarga,
                BigDecimal.valueOf(margem * 100).setScale(2, RoundingMode.HALF_UP));
    }

    public double calcularMargem(double estoqueToneladas) {
        if (estoqueToneladas >= ESTOQUE_ALTO) return MARGEM_MINIMA;
        if (estoqueToneladas <= ESTOQUE_BAIXO) return MARGEM_MAXIMA;
        double ratio = (estoqueToneladas - ESTOQUE_BAIXO) / (ESTOQUE_ALTO - ESTOQUE_BAIXO);
        return MARGEM_MAXIMA - ratio * (MARGEM_MAXIMA - MARGEM_MINIMA);
    }

    public boolean isEstabilizado(List<Double> pesos) {
        if (pesos == null || pesos.size() < windowSize) return false;
        double max = pesos.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double min = pesos.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        return (max - min) <= varianceThreshold;
    }
}
