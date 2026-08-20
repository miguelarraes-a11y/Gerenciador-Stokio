package com.senai.pi.stokio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.senai.pi.stokio.models.Materiais;

@Repository
public interface MateriaisRepository extends JpaRepository<Materiais, Integer> {
    
}

