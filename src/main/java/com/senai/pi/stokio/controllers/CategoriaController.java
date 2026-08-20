package com.senai.pi.stokio.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.senai.pi.stokio.models.Categoria;
import com.senai.pi.stokio.services.CategoriaService;

@RestController
@RequestMapping("/categoria")
@CrossOrigin(origins = "*")
public class CategoriaController {

    @Autowired
    public CategoriaService categoriaService;

    @GetMapping
    public List<Categoria> listar() {
        return categoriaService.listar();
    }

    @GetMapping("/{id}")
    public Categoria buscar(@PathVariable Integer id) {
        return categoriaService.buscarPorId(id);
    }

    @PostMapping
    public Categoria criar(@RequestBody Categoria categoria) {
        return categoriaService.salvar(categoria);
    }

    @PutMapping("/{id}")
    public Categoria atualizar(@PathVariable Integer id, @RequestBody Categoria categoria) {
        categoria.setId(id);
        return categoriaService.salvar(categoria);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        categoriaService.deletar(id);
    }
}