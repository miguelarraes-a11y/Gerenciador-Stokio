package com.senai.pi.stokio.controllers;

import com.senai.pi.stokio.models.Kit;
import com.senai.pi.stokio.services.KitService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Kit> criar(@RequestBody Map<String, Object> payload) {
        try {
            Kit kit = kitService.salvarOuAtualizarKit(null, payload);
            return ResponseEntity.ok(kit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Kit> atualizar(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        try {
            Kit kit = kitService.salvarOuAtualizarKit(id, payload);
            return ResponseEntity.ok(kit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        kitService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
