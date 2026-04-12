package br.com.test.graintransport.grain_transport_api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tipo_grao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoGrao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "preco_por_tonelada", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoPorTonelada;

    @Column(name = "estoque_toneladas", nullable = false)
    private Double estoqueToneladas;
}
