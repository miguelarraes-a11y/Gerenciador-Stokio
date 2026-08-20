package com.senai.pi.stokio.repositories;

import com.senai.pi.stokio.models.AgendamentoLaboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendamentoLaboratorioRepository extends JpaRepository<AgendamentoLaboratorio, Integer> {
}
