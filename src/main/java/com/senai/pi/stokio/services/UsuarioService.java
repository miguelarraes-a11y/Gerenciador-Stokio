package com.senai.pi.stokio.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.senai.pi.stokio.models.Usuario;
import com.senai.pi.stokio.repositories.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    public UsuarioRepository usuarioRepository;

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Usuario atualizar(Integer id, Usuario usuario) {
        Usuario existente = buscarPorId(id);
        if (existente == null) return null;
        if (usuario.getNome() != null) existente.setNome(usuario.getNome());
        if (usuario.getEmail() != null) existente.setEmail(usuario.getEmail());
        if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) existente.setSenha(usuario.getSenha());
        if (usuario.getTipoUsuario() != null) existente.setTipoUsuario(usuario.getTipoUsuario());
        if (usuario.getUf() != null) existente.setUf(usuario.getUf());
        if (usuario.getFotoUrl() != null) existente.setFotoUrl(usuario.getFotoUrl());
        if (usuario.getTelefone() != null) existente.setTelefone(usuario.getTelefone());
        return usuarioRepository.save(existente);
    }

    public void deletar(Integer id) {
        usuarioRepository.deleteById(id);
    }
}
