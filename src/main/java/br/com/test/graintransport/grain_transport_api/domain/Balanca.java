package br.com.test.graintransport.grain_transport_api.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "balanca")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Balanca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "api_key", nullable = false, unique = true, length = 100)
    private String apiKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "filial_id", nullable = false)
    private Filial filial;
}
