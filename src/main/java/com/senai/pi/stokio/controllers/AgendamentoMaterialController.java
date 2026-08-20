package com.senai.pi.stokio.controllers;

import com.senai.pi.stokio.models.AgendamentoMaterial;
import com.senai.pi.stokio.repositories.AgendamentoMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agendamentos-materiais")
@CrossOrigin(origins = "*")
public class AgendamentoMaterialController {

    @Autowired
    private AgendamentoMaterialRepository repo;

    @GetMapping
    public List<AgendamentoMaterial> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoMaterial> buscar(@PathVariable Integer id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AgendamentoMaterial> criar(@RequestBody AgendamentoMaterial ag) {
        if (ag.getStatus() == null) ag.setStatus("PENDENTE");
        AgendamentoMaterial salvo = repo.save(ag);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AgendamentoMaterial> atualizarStatus(@PathVariable Integer id, @RequestParam String status) {
        return repo.findById(id).map(ag -> {
            ag.setStatus(status);
            return ResponseEntity.ok(repo.save(ag));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
