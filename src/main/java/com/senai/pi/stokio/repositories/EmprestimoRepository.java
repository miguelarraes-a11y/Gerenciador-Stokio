package com.senai.pi.stokio.repositories;

import com.senai.pi.stokio.models.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Integer> {
}
