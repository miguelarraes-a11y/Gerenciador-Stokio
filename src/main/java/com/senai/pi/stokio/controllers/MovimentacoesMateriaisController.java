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

import com.senai.pi.stokio.models.MovimentacoesMateriais;
import com.senai.pi.stokio.services.MovimentacoesMateriaisService;

@RestController
@RequestMapping("/movimentacoes_materiais")
@CrossOrigin(origins = "*")
public class MovimentacoesMateriaisController {

    @Autowired
    public MovimentacoesMateriaisService movimentacoesMateriaisService;
    
    @GetMapping
    public List<MovimentacoesMateriais> listar() {
        return movimentacoesMateriaisService.listar();
    }

    @GetMapping("/{id}")
    public MovimentacoesMateriais buscar(@PathVariable Integer id) {
        return movimentacoesMateriaisService.buscarPorId(id);
    }

    @PostMapping
    public MovimentacoesMateriais criar(@RequestBody MovimentacoesMateriais mov) {
        if (mov.getDataMovimentacao() == null) {
            mov.setDataMovimentacao(LocalDateTime.now());
        }
        return movimentacoesMateriaisService.salvar(mov);
    }

    @PutMapping("/{id}")
    public MovimentacoesMateriais atualizar(@PathVariable Integer id, @RequestBody MovimentacoesMateriais mov) {
        mov.setId(id);
        return movimentacoesMateriaisService.salvar(mov);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        movimentacoesMateriaisService.deletar(id);
    }
}
