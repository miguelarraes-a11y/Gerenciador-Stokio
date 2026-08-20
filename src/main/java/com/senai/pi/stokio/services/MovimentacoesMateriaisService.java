package com.senai.pi.stokio.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.senai.pi.stokio.models.MovimentacoesMateriais;
import com.senai.pi.stokio.repositories.MovimentacoesMateriaisRepository;


@Service
public class MovimentacoesMateriaisService {

    @Autowired
    public MovimentacoesMateriaisRepository movimentacoesMateriaisRepository;

    public List<MovimentacoesMateriais> listar() {
        return movimentacoesMateriaisRepository.findAll();
    }

    public MovimentacoesMateriais buscarPorId(Integer id) {
        return movimentacoesMateriaisRepository.findById(id).get();
    }

    public MovimentacoesMateriais salvar(MovimentacoesMateriais mov) {
        mov.setDataMovimentacao(LocalDateTime.now());
        return movimentacoesMateriaisRepository.save(mov);
    }

    public MovimentacoesMateriais atualizar(Integer id, MovimentacoesMateriais mov) {
        MovimentacoesMateriais existente = buscarPorId(id);
        existente.setTipoMovimentacao(mov.getTipoMovimentacao());
        existente.setQuantidade(mov.getQuantidade());
        existente.setDataDevolucao(mov.getDataDevolucao());
        existente.setTempoEstimado(mov.getTempoEstimado());
        existente.setUsuarios(mov.getUsuarios());
        existente.setMateriais(mov.getMateriais());
        return movimentacoesMateriaisRepository.save(existente);
    }

    public void deletar(Integer id) {
        movimentacoesMateriaisRepository.deleteById(id);
    }
}
    
