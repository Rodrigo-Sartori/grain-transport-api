package br.com.test.graintransport.grain_transport_api.repository;

import br.com.test.graintransport.grain_transport_api.domain.TipoGrao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoGraoRepository extends JpaRepository<TipoGrao, Long> {
}
