package br.com.test.graintransport.grain_transport_api.repository;

import br.com.test.graintransport.grain_transport_api.domain.TransacaoTransporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoTransporteRepository extends JpaRepository<TransacaoTransporte, Long> {

    @Query("SELECT t FROM TransacaoTransporte t WHERE t.finalizadaEm IS NULL")
    List<TransacaoTransporte> findAllAbertas();

    @Query("SELECT t FROM TransacaoTransporte t WHERE t.caminhao.id = :caminhaoId AND t.finalizadaEm IS NULL")
    List<TransacaoTransporte> findAbertasByCaminhao(@Param("caminhaoId") Long caminhaoId);

    @Query("SELECT t FROM TransacaoTransporte t WHERE t.caminhao.placa = :placa AND t.finalizadaEm IS NULL ORDER BY t.iniciadaEm DESC")
    Optional<TransacaoTransporte> findAbertaByPlaca(@Param("placa") String placa);
}
