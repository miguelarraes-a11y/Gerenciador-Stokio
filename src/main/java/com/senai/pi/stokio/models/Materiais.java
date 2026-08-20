package com.senai.pi.stokio.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="materiais")
public class Materiais {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="nome", nullable=false)
    private String nome;

    @Column(name="descricao", length=1000)
    private String descricao;

    @Column(name="quantidade")
    private int quantidade = 10;

    @Column(name="quantidade_total")
    private int quantidadeTotal = 10;

    @Column(name="quantidade_disponivel")
    private int quantidadeDisponivel = 10;

    @Column(name="quantidade_emprestada")
    private int quantidadeEmprestada = 0;

    @Column(name="status")
    private String status = "Disponível"; // Disponível, Em Uso, Manutenção, Indisponível

    @Column(name="imagem_url", length=1000)
    private String imagemUrl;

    @Column(name="data_cadastro", nullable=false, updatable=false)
    private LocalDateTime dataCadastro;

    @Column(name="ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;

    @Column(name="estoque_minimo")
    private int estoqueMinimo = 5;

    @Column(name="estado_conservacao")
    private String estadoConservacao = "Bom";

    @Column(name="observacoes", length=1000)
    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categorias;

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
        this.ultimaAtualizacao = LocalDateTime.now();
        if (this.nome == null && this.descricao != null) {
            this.nome = this.descricao;
        }
        if (this.descricao == null && this.nome != null) {
            this.descricao = this.nome;
        }
        if (this.quantidadeTotal <= 0 && this.quantidade > 0) {
            this.quantidadeTotal = this.quantidade;
        }
        if (this.quantidadeDisponivel <= 0 && this.quantidade > 0) {
            this.quantidadeDisponivel = this.quantidade;
        }
        if (this.status == null) {
            this.status = this.quantidadeDisponivel > 0 ? "Disponível" : "Indisponível";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.ultimaAtualizacao = LocalDateTime.now();
        this.quantidade = this.quantidadeDisponivel;
        if (this.quantidadeDisponivel <= 0) {
            this.status = "Indisponível";
        } else if ("Indisponível".equals(this.status) && this.quantidadeDisponivel > 0) {
            this.status = "Disponível";
        }
    }

    public Materiais() {
    }

    public Materiais(Integer id, String nome, String descricao, int quantidadeTotal, Categoria categorias) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.quantidadeTotal = quantidadeTotal;
        this.quantidadeDisponivel = quantidadeTotal;
        this.quantidade = quantidadeTotal;
        this.quantidadeEmprestada = 0;
        this.categorias = categorias;
        this.status = quantidadeTotal > 0 ? "Disponível" : "Indisponível";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome != null ? nome : descricao;
    }

    public void setNome(String nome) {
        this.nome = nome;
        if (this.descricao == null) this.descricao = nome;
    }

    public String getDescricao() {
        return descricao != null ? descricao : nome;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
        if (this.nome == null) this.nome = descricao;
    }

    public int getQuantidade() {
        return quantidadeDisponivel;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
        this.quantidadeDisponivel = quantidade;
        if (this.quantidadeTotal < quantidade) {
            this.quantidadeTotal = quantidade;
        }
    }

    public int getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public void setQuantidadeTotal(int quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    public int getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public void setQuantidadeDisponivel(int quantidadeDisponivel) {
        this.quantidadeDisponivel = quantidadeDisponivel;
        this.quantidade = quantidadeDisponivel;
    }

    public int getQuantidadeEmprestada() {
        return quantidadeEmprestada;
    }

    public void setQuantidadeEmprestada(int quantidadeEmprestada) {
        this.quantidadeEmprestada = quantidadeEmprestada;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(int estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public String getEstadoConservacao() {
        return estadoConservacao;
    }

    public void setEstadoConservacao(String estadoConservacao) {
        this.estadoConservacao = estadoConservacao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Categoria getCategorias() {
        return categorias;
    }

    public void setCategorias(Categoria categorias) {
        this.categorias = categorias;
    }
}