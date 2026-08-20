package com.senai.pi.stokio.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.senai.pi.stokio.models.Categoria;
import com.senai.pi.stokio.repositories.CategoriaRepository;

@Service
public class CategoriaService {

    @Autowired
    public CategoriaRepository categoriaRepository;

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Integer id) {
        return categoriaRepository.findById(id).get();
    }

    public Categoria salvar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }
  
    public Categoria atualizar(Integer id, Categoria categoria) {
        Categoria existente = buscarPorId(id);
        existente.setNome(categoria.getNome());
        return categoriaRepository.save(existente);
    }

    public void deletar(Integer id) {
        categoriaRepository.deleteById(id);
    }
}
    

