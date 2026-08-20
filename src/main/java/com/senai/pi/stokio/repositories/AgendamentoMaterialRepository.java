package com.senai.pi.stokio.repositories;

import com.senai.pi.stokio.models.AgendamentoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendamentoMaterialRepository extends JpaRepository<AgendamentoMaterial, Integer> {
}
