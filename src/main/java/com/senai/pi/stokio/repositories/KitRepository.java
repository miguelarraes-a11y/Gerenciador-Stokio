package com.senai.pi.stokio.repositories;

import com.senai.pi.stokio.models.Kit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KitRepository extends JpaRepository<Kit, Integer> {
}
