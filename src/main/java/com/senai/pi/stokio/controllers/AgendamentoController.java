package com.senai.pi.stokio.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.senai.pi.stokio.models.Agendamento;
import com.senai.pi.stokio.services.AgendamentoService;

@RestController
@RequestMapping("/agendamentos")
@CrossOrigin(origins = "*")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

    @GetMapping
    public List<Agendamento> listar(@RequestParam(required = false) String status) {
        if (status != null && !status.trim().isEmpty()) {
            return agendamentoService.listarPorStatus(status.toUpperCase());
        }
        return agendamentoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        return agendamentoService.buscarPorId(id)
                .map(agendamento -> ResponseEntity.ok().body((Object) agendamento))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Agendamento não encontrado com o ID: " + id)));
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Agendamento agendamento) {
        try {
            Agendamento criado = agendamentoService.criar(agendamento);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("erro", "Erro interno ao processar agendamento: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @RequestBody Agendamento agendamento) {
        try {
            Agendamento atualizado = agendamentoService.atualizar(id, agendamento);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            String novoStatus = body.get("status");
            if (novoStatus == null || novoStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "O campo status é obrigatório."));
            }
            Agendamento atualizado = agendamentoService.atualizarStatus(id, novoStatus);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Integer id) {
        try {
            agendamentoService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Não foi possível excluir o agendamento: " + e.getMessage()));
        }
    }
}
