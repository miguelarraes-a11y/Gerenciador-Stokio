package com.senai.pi.stokio.repositories;

import com.senai.pi.stokio.models.SolicitacaoKit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoKitRepository extends JpaRepository<SolicitacaoKit, Integer> {
    List<SolicitacaoKit> findBySolicitanteIdOrderByDataSolicitacaoDesc(Integer solicitanteId);
    List<SolicitacaoKit> findAllByOrderByDataSolicitacaoDesc();
    List<SolicitacaoKit> findByStatusOrderByDataSolicitacaoDesc(String status);
    boolean existsByNomeIgnoreCaseAndStatus(String nome, String status);
}
