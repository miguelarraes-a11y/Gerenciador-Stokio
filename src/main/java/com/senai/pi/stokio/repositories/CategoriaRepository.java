package com.senai.pi.stokio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.senai.pi.stokio.models.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    
}
