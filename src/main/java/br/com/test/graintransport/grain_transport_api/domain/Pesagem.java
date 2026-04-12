package br.com.test.graintransport.grain_transport_api.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pesagem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pesagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transacao_id", nullable = false)
    private TransacaoTransporte transacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "balanca_id", nullable = false)
    private Balanca balanca;

    @Column(name = "peso_bruto", nullable = false)
    private Double pesoBruto;

    @Column(nullable = false)
    private Double tara;

    @Column(name = "peso_liquido", nullable = false)
    private Double pesoLiquido;

    @Column(name = "custo_carga", nullable = false, precision = 15, scale = 2)
    private BigDecimal custoCarga;

    @Column(name = "margem_aplicada", nullable = false, precision = 5, scale = 4)
    private BigDecimal margemAplicada;

    @Column(name = "pesado_em", nullable = false)
    private LocalDateTime pesadoEm;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
