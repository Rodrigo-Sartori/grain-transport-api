package br.com.test.graintransport.grain_transport_api.repository;

import br.com.test.graintransport.grain_transport_api.domain.Balanca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BalancaRepository extends JpaRepository<Balanca, Long> {

    Optional<Balanca> findByApiKey(String apiKey);

    Optional<Balanca> findByCodigo(String codigo);
}
