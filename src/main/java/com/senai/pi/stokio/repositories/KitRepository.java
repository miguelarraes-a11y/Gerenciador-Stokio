package com.senai.pi.stokio.repositories;

import com.senai.pi.stokio.models.Kit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KitRepository extends JpaRepository<Kit, Integer> {
    List<Kit> findByStatusNotOrStatusIsNull(String status);
    Optional<Kit> findByNomeIgnoreCase(String nome);
    Optional<Kit> findByNomeIgnoreCaseAndStatusNot(String nome, String status);
    boolean existsByNomeIgnoreCaseAndStatusNot(String nome, String status);
}
