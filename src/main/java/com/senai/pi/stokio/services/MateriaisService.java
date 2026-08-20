package com.senai.pi.stokio.services;

import com.senai.pi.stokio.repositories.MateriaisRepository;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.senai.pi.stokio.models.Materiais;

@Service
public class MateriaisService {

    @Autowired
    public MateriaisRepository materiaisRepository;

    public List<Materiais> listar() {
        return materiaisRepository.findAll();
    }

    public Materiais buscarPorId(Integer id) {
        return materiaisRepository.findById(id).orElseThrow(() -> new RuntimeException("Material não encontrado"));
    }

    public Materiais salvar(Materiais material) {
        return materiaisRepository.save(material);
    }

    public Materiais atualizar(Integer id, Materiais material) {
        Materiais existente = buscarPorId(id);
        existente.setDescricao(material.getDescricao());
        existente.setQuantidade(material.getQuantidade());
        existente.setCategorias(material.getCategorias());
        if (material.getEstadoConservacao() != null) existente.setEstadoConservacao(material.getEstadoConservacao());
        if (material.getEstoqueMinimo() > 0) existente.setEstoqueMinimo(material.getEstoqueMinimo());
        if (material.getImagemUrl() != null) existente.setImagemUrl(material.getImagemUrl());
        if (material.getObservacoes() != null) existente.setObservacoes(material.getObservacoes());
        return materiaisRepository.save(existente);
    }

    public void deletar(Integer id) {
        materiaisRepository.deleteById(id);
    }
}
