package com.senai.pi.stokio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.senai.pi.stokio.models.MovimentacoesMateriais;

@Repository
public interface MovimentacoesMateriaisRepository extends JpaRepository<MovimentacoesMateriais, Integer> {
    
}

