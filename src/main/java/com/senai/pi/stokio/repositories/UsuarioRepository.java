package com.senai.pi.stokio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.senai.pi.stokio.models.Usuario;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmailAndSenha(String email, String senha);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByTipoUsuarioIgnoreCase(String tipoUsuario);
    List<Usuario> findByTipoUsuarioInIgnoreCase(List<String> tipos);
}
