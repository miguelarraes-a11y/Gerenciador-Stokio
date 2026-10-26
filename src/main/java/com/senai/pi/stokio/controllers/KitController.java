package com.senai.pi.stokio.controllers;

import com.senai.pi.stokio.models.Kit;
import com.senai.pi.stokio.services.KitService;
import com.senai.pi.stokio.models.Usuario;
import com.senai.pi.stokio.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/kits")
@CrossOrigin(origins = "*")
public class KitController {

    @Autowired
    private KitService kitService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<Kit> listarTodos() {
        return kitService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Kit> buscarPorId(@PathVariable Integer id) {
        return kitService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestParam(required = false) Integer userId,
            @RequestBody Map<String, Object> payload) {
        if (userId != null) {
            Usuario u = usuarioRepository.findById(userId).orElse(null);
            if (u != null && !u.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Apenas administradores podem cadastrar kits diretamente. Professores devem solicitar novos kits pedagógicos para aprovação."));
            }
        }
        try {
            Kit kit = kitService.salvarOuAtualizarKit(null, payload);
            return ResponseEntity.ok(kit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer userId,
            @RequestBody Map<String, Object> payload) {
        if (userId != null) {
            Usuario u = usuarioRepository.findById(userId).orElse(null);
            if (u != null && !u.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Apenas administradores podem editar kits existentes."));
            }
        }
        try {
            Kit kit = kitService.salvarOuAtualizarKit(id, payload);
            return ResponseEntity.ok(kit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer userId) {
        if (userId != null) {
            Usuario u = usuarioRepository.findById(userId).orElse(null);
            if (u != null && !u.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Apenas administradores podem excluir kits do sistema."));
            }
        }
        kitService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
