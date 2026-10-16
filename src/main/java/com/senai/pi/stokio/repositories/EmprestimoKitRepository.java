package com.senai.pi.stokio.repositories;

import com.senai.pi.stokio.models.EmprestimoKit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmprestimoKitRepository extends JpaRepository<EmprestimoKit, Integer> {
    List<EmprestimoKit> findBySolicitanteId(Integer usuarioId);
    List<EmprestimoKit> findByStatus(String status);
    boolean existsByKitId(Integer kitId);
}
