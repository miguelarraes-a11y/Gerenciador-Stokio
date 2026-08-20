package com.senai.pi.stokio.models;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="movimentacoes_materiais")
public class MovimentacoesMateriais {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="tipo_movimentacao")
    private String tipoMovimentacao;

    @Column(name="quantidade")
    private int quantidade;

    @Column(name="data_movimentacao")
    private LocalDateTime dataMovimentacao;

    @Column(name="data_devolucao")
    private LocalDateTime dataDevolucao;

    @Column(name="tempo_estimado")
    private int tempoEstimado;
 
    @ManyToOne
    @JoinColumn(name = "usuarios_id")
    private Usuario usuarios;

    @ManyToMany
     @JoinTable(
        name = "movimentacoes_materiais_materiais",
        joinColumns = @JoinColumn(name = "movimentacoes_materiais_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "materiais_id", referencedColumnName = "id")
    )
    private Set<Materiais> materiais;

    public MovimentacoesMateriais() {
    }

    public MovimentacoesMateriais(Integer id, String tipoMovimentacao, int quantidade, LocalDateTime dataMovimentacao,
            LocalDateTime dataDevolucao, int tempoEstimado, Usuario usuarios, Set<Materiais> materiais) {
        this.id = id;
        this.tipoMovimentacao = tipoMovimentacao;
        this.quantidade = quantidade;
        this.dataMovimentacao = dataMovimentacao;
        this.dataDevolucao = dataDevolucao;
        this.tempoEstimado = tempoEstimado;
        this.usuarios = usuarios;
        this.materiais = materiais;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipoMovimentacao() {
        return tipoMovimentacao;
    }

    public void setTipoMovimentacao(String tipoMovimentacao) {
        this.tipoMovimentacao = tipoMovimentacao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getDataMovimentacao() {
        return dataMovimentacao;
    }

    public void setDataMovimentacao(LocalDateTime dataMovimentacao) {
        this.dataMovimentacao = dataMovimentacao;
    }

    public LocalDateTime getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(LocalDateTime dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public int getTempoEstimado() {
        return tempoEstimado;
    }

    public void setTempoEstimado(int tempoEstimado) {
        this.tempoEstimado = tempoEstimado;
    }

    public Usuario getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Usuario usuarios) {
        this.usuarios = usuarios;
    }

    public Set<Materiais> getMateriais() {
        return materiais;
    }

    public void setMateriais(Set<Materiais> materiais) {
        this.materiais = materiais;
    }


}