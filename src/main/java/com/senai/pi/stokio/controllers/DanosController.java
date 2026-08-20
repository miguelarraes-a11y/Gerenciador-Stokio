package com.senai.pi.stokio.controllers;

import java.time.LocalDateTime;
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

import com.senai.pi.stokio.models.Danos;
import com.senai.pi.stokio.services.DanosService;

@RestController
@RequestMapping("/danos")
@CrossOrigin(origins = "*")
public class DanosController {

    @Autowired
    public DanosService danosService;

    @GetMapping
    public List<Danos> listar() {
        return danosService.listar();
    }

    @GetMapping("/{id}")
    public Danos buscar(@PathVariable Integer id) {
        return danosService.buscarPorId(id);
    }

    @PostMapping
    public Danos criar(@RequestBody Danos dano) {
        if (dano.getDataRegistro() == null) {
            dano.setDataRegistro(LocalDateTime.now());
        }
        return danosService.salvar(dano);
    }

    @PutMapping("/{id}")
    public Danos atualizar(@PathVariable Integer id, @RequestBody Danos dano) {
        dano.setId(id);
        return danosService.salvar(dano);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        danosService.deletar(id);
    }
}
