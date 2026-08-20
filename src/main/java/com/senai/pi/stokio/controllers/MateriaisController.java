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
import org.springframework.web.bind.annotation.RestController;

import com.senai.pi.stokio.models.Materiais;
import com.senai.pi.stokio.services.MateriaisService;

@RestController
@RequestMapping("/materiais")
@CrossOrigin(origins = "*")
public class MateriaisController {

    @Autowired
    public MateriaisService materiaisService;

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
    public ResponseEntity<?> criar(@RequestBody Materiais material) {
        try {
            Materiais criado = materiaisService.salvar(material);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @RequestBody Materiais material) {
        try {
            Materiais atualizado = materiaisService.atualizar(id, material);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Integer id) {
        try {
            materiaisService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
        }
    }
}
