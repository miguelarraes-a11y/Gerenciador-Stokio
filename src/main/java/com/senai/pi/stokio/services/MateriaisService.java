package com.senai.pi.stokio.services;

import com.senai.pi.stokio.models.Materiais;
import com.senai.pi.stokio.models.MovimentacoesMateriais;
import com.senai.pi.stokio.models.Usuario;
import com.senai.pi.stokio.repositories.EmprestimoRepository;
import com.senai.pi.stokio.repositories.MateriaisRepository;
import com.senai.pi.stokio.repositories.MovimentacoesMateriaisRepository;
import com.senai.pi.stokio.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MateriaisService {

    @Autowired
    public MateriaisRepository materiaisRepository;

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private MovimentacoesMateriaisRepository movimentacoesMateriaisRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Materiais> listar() {
        return materiaisRepository.findAll().stream()
                .filter(m -> !"Excluído".equalsIgnoreCase(m.getStatus()))
                .collect(Collectors.toList());
    }

    public Materiais buscarPorId(Integer id) {
        return materiaisRepository.findById(id)
                .filter(m -> !"Excluído".equalsIgnoreCase(m.getStatus()))
                .orElseThrow(() -> new RuntimeException("Material não encontrado com o ID: " + id));
    }

    public Materiais salvar(Materiais material) {
        if (material.getStatus() == null) {
            material.setStatus(material.getQuantidadeDisponivel() > 0 ? "Disponível" : "Indisponível");
        }
        return materiaisRepository.save(material);
    }

    public Materiais atualizar(Integer id, Materiais material) {
        Materiais existente = buscarPorId(id);
        if (material.getNome() != null) existente.setNome(material.getNome());
        if (material.getDescricao() != null) existente.setDescricao(material.getDescricao());
        existente.setQuantidade(material.getQuantidade());
        existente.setCategorias(material.getCategorias());
        if (material.getEstadoConservacao() != null) existente.setEstadoConservacao(material.getEstadoConservacao());
        if (material.getEstoqueMinimo() > 0) existente.setEstoqueMinimo(material.getEstoqueMinimo());
        if (material.getImagemUrl() != null) existente.setImagemUrl(material.getImagemUrl());
        if (material.getObservacoes() != null) existente.setObservacoes(material.getObservacoes());
        return materiaisRepository.save(existente);
    }

    @Transactional
    public Materiais darBaixa(Integer id, int quantidade, Integer usuarioId, String motivo) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade informada para baixa deve ser maior que zero.");
        }

        Materiais mat = buscarPorId(id);

        if (quantidade > mat.getQuantidadeDisponivel()) {
            throw new IllegalArgumentException("A quantidade informada (" + quantidade + ") não pode ser superior à quantidade disponível (" + mat.getQuantidadeDisponivel() + ").");
        }

        int novaQtdDisponivel = mat.getQuantidadeDisponivel() - quantidade;
        mat.setQuantidadeDisponivel(novaQtdDisponivel);
        mat.setQuantidade(novaQtdDisponivel);
        mat.setQuantidadeTotal(Math.max(0, mat.getQuantidadeTotal() - quantidade));

        if (novaQtdDisponivel <= 0) {
            mat.setStatus("Indisponível");
        }

        // Registrar movimentação de baixa para rastreabilidade
        Usuario usuario = null;
        if (usuarioId != null) {
            usuario = usuarioRepository.findById(usuarioId).orElse(null);
        }

        MovimentacoesMateriais mov = new MovimentacoesMateriais();
        mov.setTipoMovimentacao("BAIXA");
        mov.setQuantidade(quantidade);
        mov.setDataMovimentacao(LocalDateTime.now());
        mov.setUsuarios(usuario);
        mov.setMateriais(new HashSet<>(Collections.singletonList(mat)));
        movimentacoesMateriaisRepository.save(mov);

        return materiaisRepository.save(mat);
    }

    @Transactional
    public Materiais repor(Integer id, int quantidade, Integer usuarioId, String motivo) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade informada para reposição deve ser maior que zero.");
        }

        Materiais mat = buscarPorId(id);

        int novaQtdDisponivel = mat.getQuantidadeDisponivel() + quantidade;
        mat.setQuantidadeDisponivel(novaQtdDisponivel);
        mat.setQuantidade(novaQtdDisponivel);
        mat.setQuantidadeTotal(mat.getQuantidadeTotal() + quantidade);
        mat.setStatus("Disponível");

        // Registrar movimentação de reposição
        Usuario usuario = null;
        if (usuarioId != null) {
            usuario = usuarioRepository.findById(usuarioId).orElse(null);
        }

        MovimentacoesMateriais mov = new MovimentacoesMateriais();
        mov.setTipoMovimentacao("REPOSICAO");
        mov.setQuantidade(quantidade);
        mov.setDataMovimentacao(LocalDateTime.now());
        mov.setUsuarios(usuario);
        mov.setMateriais(new HashSet<>(Collections.singletonList(mat)));
        movimentacoesMateriaisRepository.save(mov);

        return materiaisRepository.save(mat);
    }

    @Transactional
    public void deletar(Integer id) {
        Materiais mat = materiaisRepository.findById(id).orElse(null);
        if (mat == null) return;

        // Impedir exclusão caso haja empréstimos ativos pendentes ou retirados
        if (emprestimoRepository.existsByMaterialIdAndStatusIn(id, List.of("Pendente", "Aprovado", "Retirado"))) {
            throw new IllegalStateException("Não é possível excluir o material pois existem empréstimos ativos vinculados a ele.");
        }

        mat.setStatus("Excluído");
        mat.setQuantidadeDisponivel(0);
        mat.setQuantidade(0);
        materiaisRepository.save(mat);
    }
}
