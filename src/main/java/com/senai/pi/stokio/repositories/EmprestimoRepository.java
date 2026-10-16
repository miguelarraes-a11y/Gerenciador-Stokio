package com.senai.pi.stokio.repositories;

import com.senai.pi.stokio.models.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Integer> {
    boolean existsByMaterialId(Integer materialId);
    boolean existsByMaterialIdAndStatusIn(Integer materialId, List<String> statuses);
}
