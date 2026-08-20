package com.senai.pi.stokio.repositories;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.senai.pi.stokio.models.Agendamento;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {

    List<Agendamento> findByStatus(String status);

    List<Agendamento> findByUsuarioId(Integer usuarioId);

    List<Agendamento> findByMaterialId(Integer materialId);

    @Query("SELECT a FROM Agendamento a WHERE a.material.id = :materialId " +
           "AND a.status != 'CANCELADO' " +
           "AND (a.dataInicio < :dataFim AND a.dataFim > :dataInicio) " +
           "AND (:idIsNotNull = false OR a.id != :agendamentoId)")
    List<Agendamento> findConflitos(
            @Param("materialId") Integer materialId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("agendamentoId") Integer agendamentoId,
            @Param("idIsNotNull") boolean idIsNotNull
    );
}
