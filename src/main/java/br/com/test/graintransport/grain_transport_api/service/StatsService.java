package br.com.test.graintransport.grain_transport_api.service;

import br.com.test.graintransport.grain_transport_api.domain.GroupBy;
import br.com.test.graintransport.grain_transport_api.domain.Pesagem;
import br.com.test.graintransport.grain_transport_api.dto.CustosStatsDTO;
import br.com.test.graintransport.grain_transport_api.dto.GrupoStatDTO;
import br.com.test.graintransport.grain_transport_api.dto.LucroStatsDTO;
import br.com.test.graintransport.grain_transport_api.dto.PesagensStatsDTO;
import br.com.test.graintransport.grain_transport_api.repository.PesagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final PesagemRepository pesagemRepository;

    @Transactional(readOnly = true)
    public PesagensStatsDTO pesagens(Long filialId, Long caminhaoId, Long tipoGraoId,
                                     LocalDate inicio, LocalDate fim, GroupBy groupBy) {
        var pesagens = buscar(filialId, caminhaoId, tipoGraoId, inicio, fim);

        var pesoBruto   = pesagens.stream().mapToDouble(Pesagem::getPesoBruto).sum();
        var pesoLiquido = pesagens.stream().mapToDouble(Pesagem::getPesoLiquido).sum();

        return new PesagensStatsDTO(
                pesagens.size(),
                pesoBruto,
                pesoLiquido,
                agrupar(pesagens, groupBy, p -> BigDecimal.valueOf(p.getPesoLiquido()))
        );
    }

    @Transactional(readOnly = true)
    public CustosStatsDTO custos(Long filialId, Long caminhaoId, Long tipoGraoId,
                                  LocalDate inicio, LocalDate fim, GroupBy groupBy) {
        var pesagens = buscar(filialId, caminhaoId, tipoGraoId, inicio, fim);

        var custoTotal = pesagens.stream()
                .map(Pesagem::getCustoCarga)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        var custoMedio = pesagens.isEmpty() ? BigDecimal.ZERO
                : custoTotal.divide(BigDecimal.valueOf(pesagens.size()), 2, RoundingMode.HALF_UP);

        return new CustosStatsDTO(
                custoTotal,
                custoMedio,
                agrupar(pesagens, groupBy, Pesagem::getCustoCarga)
        );
    }

    @Transactional(readOnly = true)
    public LucroStatsDTO lucros(Long filialId, Long caminhaoId, Long tipoGraoId,
                                 LocalDate inicio, LocalDate fim, GroupBy groupBy) {
        var pesagens = buscar(filialId, caminhaoId, tipoGraoId, inicio, fim);

        var lucroTotal = pesagens.stream()
                .map(p -> p.getCustoCarga().multiply(p.getMargemAplicada()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        var margemMediaPercent = pesagens.isEmpty() ? BigDecimal.ZERO
                : pesagens.stream()
                        .map(Pesagem::getMargemAplicada)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(pesagens.size()), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        return new LucroStatsDTO(
                lucroTotal,
                margemMediaPercent,
                agrupar(pesagens, groupBy, p -> p.getCustoCarga().multiply(p.getMargemAplicada()))
        );
    }
    private List<Pesagem> buscar(Long filialId, Long caminhaoId, Long tipoGraoId,
                                  LocalDate inicio, LocalDate fim) {
        return pesagemRepository.findByFiltro(
                filialId,
                caminhaoId,
                tipoGraoId,
                inicio != null ? inicio.atStartOfDay() : null,
                fim    != null ? fim.atTime(23, 59, 59) : null
        );
    }

    private List<GrupoStatDTO> agrupar(List<Pesagem> pesagens, GroupBy groupBy,
                                        Function<Pesagem, BigDecimal> valorFn) {
        Function<Pesagem, String> labelFn = switch (groupBy) {
            case FILIAL   -> p -> p.getTransacao().getFilial().getNome();
            case CAMINHAO -> p -> p.getTransacao().getCaminhao().getPlaca();
            case GRAO     -> p -> p.getTransacao().getTipoGrao().getNome();
            case PERIODO  -> p -> p.getPesadoEm().toLocalDate().toString().substring(0, 7);
        };

        Map<String, List<Pesagem>> grupos = pesagens.stream()
                .collect(Collectors.groupingBy(labelFn));

        return grupos.entrySet().stream()
                .map(e -> new GrupoStatDTO(
                        e.getKey(),
                        e.getValue().stream()
                                .map(valorFn)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .setScale(2, RoundingMode.HALF_UP),
                        e.getValue().size()
                ))
                .sorted((a, b) -> b.valor().compareTo(a.valor()))
                .toList();
    }
}
