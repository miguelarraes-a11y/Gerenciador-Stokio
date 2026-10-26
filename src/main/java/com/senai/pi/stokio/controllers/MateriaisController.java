package com.senai.pi.stokio.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.senai.pi.stokio.models.Materiais;
import com.senai.pi.stokio.models.Usuario;
import com.senai.pi.stokio.repositories.UsuarioRepository;
import com.senai.pi.stokio.services.MateriaisService;

@RestController
@RequestMapping("/materiais")
@CrossOrigin(origins = "*")
public class MateriaisController {

    @Autowired
    public MateriaisService materiaisService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private boolean validarAdmin(Integer userId) {
        if (userId == null) return true; // Para chamadas locais/testes sem autenticação
        Usuario u = usuarioRepository.findById(userId).orElse(null);
        return u != null && u.isAdmin();
    }

    @GetMapping
    public List<Materiais> listar() {
        return materiaisService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Integer id) {
        try {
            Materiais m = materiaisService.buscarPorId(id);
            return ResponseEntity.ok(m);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Material não encontrado com o ID: " + id));
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestParam(required = false) Integer userId,
            @RequestBody Materiais material) {
        if (!validarAdmin(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Apenas administradores podem cadastrar novos materiais no estoque."));
        }
        try {
            Materiais criado = materiaisService.salvar(material);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer userId,
            @RequestBody Materiais material) {
        if (!validarAdmin(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Apenas administradores podem editar materiais do estoque."));
        }
        try {
            Materiais atualizado = materiaisService.atualizar(id, material);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/{id}/baixa")
    public ResponseEntity<?> darBaixa(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer userId,
            @RequestBody Map<String, Object> payload) {
        Integer usuarioId = userId;
        if (payload.containsKey("usuarioId") && payload.get("usuarioId") != null) {
            Object uObj = payload.get("usuarioId");
            usuarioId = uObj instanceof Integer ? (Integer) uObj : Integer.parseInt(uObj.toString());
        }

        if (!validarAdmin(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Apenas administradores podem dar baixa direta no estoque. Professores devem solicitar empréstimos."));
        }

        try {
            Object qObj = payload.get("quantidade");
            if (qObj == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Quantidade é obrigatória."));
            }
            int quantidade = qObj instanceof Integer ? (Integer) qObj : Integer.parseInt(qObj.toString());

            String motivo = (String) payload.getOrDefault("motivo", "Baixa de material");

            Materiais atualizado = materiaisService.darBaixa(id, quantidade, usuarioId, motivo);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao realizar baixa: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/repor")
    public ResponseEntity<?> repor(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer userId,
            @RequestBody Map<String, Object> payload) {
        Integer usuarioId = userId;
        if (payload.containsKey("usuarioId") && payload.get("usuarioId") != null) {
            Object uObj = payload.get("usuarioId");
            usuarioId = uObj instanceof Integer ? (Integer) uObj : Integer.parseInt(uObj.toString());
        }

        if (!validarAdmin(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Apenas administradores podem repor estoque diretamente."));
        }

        try {
            Object qObj = payload.get("quantidade");
            if (qObj == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Quantidade é obrigatória."));
            }
            int quantidade = qObj instanceof Integer ? (Integer) qObj : Integer.parseInt(qObj.toString());

            String motivo = (String) payload.getOrDefault("motivo", "Reposição de estoque");

            Materiais atualizado = materiaisService.repor(id, quantidade, usuarioId, motivo);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao repor estoque: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer userId) {
        if (!validarAdmin(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Apenas administradores podem excluir materiais do estoque."));
        }
        try {
            materiaisService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
        }
    }
}
