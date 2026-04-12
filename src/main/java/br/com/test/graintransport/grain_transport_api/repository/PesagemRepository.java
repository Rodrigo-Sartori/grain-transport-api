package br.com.test.graintransport.grain_transport_api.repository;

import br.com.test.graintransport.grain_transport_api.domain.Pesagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PesagemRepository extends JpaRepository<Pesagem, Long> {

    List<Pesagem> findByTransacaoId(Long transacaoId);

    @Query("SELECT p FROM Pesagem p WHERE p.pesadoEm BETWEEN :inicio AND :fim")
    List<Pesagem> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT p FROM Pesagem p WHERE p.transacao.filial.id = :filialId")
    List<Pesagem> findByFilialId(@Param("filialId") Long filialId);

    @Query(value = """
            SELECT p.* FROM pesagem p
            JOIN transacao_transporte t ON p.transacao_id = t.id
            WHERE (CAST(:filialId   AS bigint)    IS NULL OR t.filial_id    = :filialId)
              AND (CAST(:caminhaoId AS bigint)    IS NULL OR t.caminhao_id  = :caminhaoId)
              AND (CAST(:tipoGraoId AS bigint)    IS NULL OR t.tipo_grao_id = :tipoGraoId)
              AND (CAST(:inicio     AS timestamp) IS NULL OR p.pesado_em   >= :inicio)
              AND (CAST(:fim        AS timestamp) IS NULL OR p.pesado_em   <= :fim)
            ORDER BY p.pesado_em DESC
            """, nativeQuery = true)
    List<Pesagem> findByFiltro(
            @Param("filialId")   Long filialId,
            @Param("caminhaoId") Long caminhaoId,
            @Param("tipoGraoId") Long tipoGraoId,
            @Param("inicio")     LocalDateTime inicio,
            @Param("fim")        LocalDateTime fim
    );
}
