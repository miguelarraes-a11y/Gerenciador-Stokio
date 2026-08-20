package com.senai.pi.stokio.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.senai.pi.stokio.models.Danos;
import com.senai.pi.stokio.repositories.DanosRepository;

@Service
public class DanosService {

    @Autowired
    public DanosRepository danosRepository;

    public List<Danos> listar() {
        return danosRepository.findAll();
    }

    public Danos buscarPorId(Integer id) {
        return danosRepository.findById(id).get();
    }
    public Danos salvar(Danos dano) {
        dano.setDataRegistro(LocalDateTime.now());
        return danosRepository.save(dano);
    }

    public Danos atualizar(Integer id, Danos dano) {
        Danos existente = buscarPorId(id);
        existente.setDescricaoDano(dano.getDescricaoDano());
        existente.setStatusResolucao(dano.getStatusResolucao());
        existente.setMateriais(dano.getMateriais());
        return danosRepository.save(existente);
    }
    
    public void deletar(Integer id) {
        danosRepository.deleteById(id);
    }
}

