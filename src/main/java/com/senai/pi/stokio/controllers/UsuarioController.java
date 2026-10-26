package com.senai.pi.stokio.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.senai.pi.stokio.models.Usuario;
import com.senai.pi.stokio.repositories.UsuarioRepository;
import com.senai.pi.stokio.services.UsuarioService;

@RestController
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    public UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ---- CRUD usuarios ----
    @GetMapping("/usuario")
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/usuario/{id}")
    public Usuario buscar(@PathVariable Integer id) {
        return usuarioService.buscarPorId(id);
    }

    @PostMapping("/usuario")
    public Usuario criar(@RequestBody Usuario usuario) {
        return usuarioService.salvar(usuario);
    }

    @PutMapping("/usuario/{id}")
    public Usuario atualizar(@PathVariable Integer id, @RequestBody Usuario usuario) {
        usuario.setId(id);
        return usuarioService.salvar(usuario);
    }

    @GetMapping("/usuario/professores")
    public List<Usuario> listarProfessores() {
        return usuarioRepository.findAll().stream()
                .filter(Usuario::isProfessor)
                .toList();
    }

    @PutMapping("/usuario/{id}/tipo")
    public ResponseEntity<?> atualizarTipo(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer adminId,
            @RequestBody Map<String, String> payload) {
        if (adminId != null) {
            Usuario caller = usuarioService.buscarPorId(adminId);
            if (caller == null || !caller.isAdmin()) {
                return ResponseEntity.status(403).body(Map.of("erro", "Apenas administradores podem alterar permissões de usuários."));
            }
        }
        String novoTipo = payload.get("tipoUsuario");
        if (novoTipo == null || novoTipo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O campo tipoUsuario é obrigatório."));
        }
        Usuario u = usuarioService.buscarPorId(id);
        if (u == null) {
            return ResponseEntity.notFound().build();
        }
        u.setTipoUsuario(novoTipo.trim());
        Usuario salvo = usuarioService.salvar(u);
        return ResponseEntity.ok(salvo);
    }

    @DeleteMapping("/usuario/{id}")
    public ResponseEntity<?> deletar(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer adminId) {
        if (adminId != null) {
            Usuario caller = usuarioService.buscarPorId(adminId);
            if (caller == null || !caller.isAdmin()) {
                return ResponseEntity.status(403).body(Map.of("erro", "Apenas administradores podem excluir usuários."));
            }
            if (adminId.equals(id)) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Não é permitido excluir o próprio usuário autenticado."));
            }
        }
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Auth endpoints ----
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String senha = credentials.get("senha");
        Optional<Usuario> usuario = usuarioRepository.findByEmailAndSenha(email, senha);
        if (usuario.isPresent()) {
            return ResponseEntity.ok(usuario.get());
        }
        return ResponseEntity.status(401).body(Map.of("erro", "Email ou senha inválidos"));
    }

    @PostMapping("/auth/cadastro")
    public ResponseEntity<?> cadastro(@RequestBody Usuario novoUsuario) {
        Optional<Usuario> existente = usuarioRepository.findByEmail(novoUsuario.getEmail());
        if (existente.isPresent()) {
            return ResponseEntity.status(409).body(Map.of("erro", "Email já cadastrado"));
        }
        if (novoUsuario.getTipoUsuario() == null) {
            novoUsuario.setTipoUsuario("aluno");
        }
        Usuario salvo = usuarioService.salvar(novoUsuario);
        return ResponseEntity.ok(salvo);
    }
}
