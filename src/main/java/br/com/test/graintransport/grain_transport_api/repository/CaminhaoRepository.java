package br.com.test.graintransport.grain_transport_api.repository;

import br.com.test.graintransport.grain_transport_api.domain.Caminhao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaminhaoRepository extends JpaRepository<Caminhao, Long> {
}
