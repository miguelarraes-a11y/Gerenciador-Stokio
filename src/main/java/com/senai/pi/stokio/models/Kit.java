package com.senai.pi.stokio.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kits")
public class Kit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "descricao", length = 1000)
    private String descricao;

    @Column(name = "finalidade", length = 1000)
    private String finalidade;

    @Column(name = "imagem_url", length = 1000)
    private String imagemUrl;

    @Column(name = "quantidade_disponivel")
    private int quantidadeDisponivel = 5;

    @Column(name = "status")
    private String status = "Disponível"; // Disponível, Em Uso, Indisponível

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @OneToMany(mappedBy = "kit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<KitItem> itens = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
        if (this.status == null) this.status = "Disponível";
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public Kit() {}

    public Kit(String nome, String descricao, String finalidade, int quantidadeDisponivel, Categoria categoria) {
        this.nome = nome;
        this.descricao = descricao;
        this.finalidade = finalidade;
        this.quantidadeDisponivel = quantidadeDisponivel;
        this.categoria = categoria;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getFinalidade() { return finalidade; }
    public void setFinalidade(String finalidade) { this.finalidade = finalidade; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    public int getQuantidadeDisponivel() { return quantidadeDisponivel; }
    public void setQuantidadeDisponivel(int quantidadeDisponivel) { this.quantidadeDisponivel = quantidadeDisponivel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public List<KitItem> getItens() { return itens; }
    public void setItens(List<KitItem> itens) { this.itens = itens; }
}
