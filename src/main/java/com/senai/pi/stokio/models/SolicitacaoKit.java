package com.senai.pi.stokio.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitacoes_kit")
public class SolicitacaoKit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "descricao", length = 1000)
    private String descricao;

    @Column(name = "finalidade", length = 1000)
    private String finalidade;

    @Column(name = "quantidade_disponivel")
    private int quantidadeDisponivel = 5;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne
    @JoinColumn(name = "aprovador_id")
    private Usuario aprovador;

    @Column(name = "status", nullable = false)
    private String status = "Pendente"; // Pendente, Aprovada, Recusada

    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_resposta")
    private LocalDateTime dataResposta;

    @Column(name = "justificativa", length = 1000)
    private String justificativa;

    @ManyToOne
    @JoinColumn(name = "kit_criado_id")
    private Kit kitCriado;

    @OneToMany(mappedBy = "solicitacaoKit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SolicitacaoKitItem> itens = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dataSolicitacao = LocalDateTime.now();
        if (this.status == null) this.status = "Pendente";
    }

    public SolicitacaoKit() {}

    public SolicitacaoKit(String nome, String descricao, String finalidade, int quantidadeDisponivel, Categoria categoria, Usuario solicitante) {
        this.nome = nome;
        this.descricao = descricao;
        this.finalidade = finalidade;
        this.quantidadeDisponivel = quantidadeDisponivel;
        this.categoria = categoria;
        this.solicitante = solicitante;
        this.status = "Pendente";
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getFinalidade() { return finalidade; }
    public void setFinalidade(String finalidade) { this.finalidade = finalidade; }

    public int getQuantidadeDisponivel() { return quantidadeDisponivel; }
    public void setQuantidadeDisponivel(int quantidadeDisponivel) { this.quantidadeDisponivel = quantidadeDisponivel; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public Usuario getSolicitante() { return solicitante; }
    public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }

    public Usuario getAprovador() { return aprovador; }
    public void setAprovador(Usuario aprovador) { this.aprovador = aprovador; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }

    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }

    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }

    public Kit getKitCriado() { return kitCriado; }
    public void setKitCriado(Kit kitCriado) { this.kitCriado = kitCriado; }

    public List<SolicitacaoKitItem> getItens() { return itens; }
    public void setItens(List<SolicitacaoKitItem> itens) { this.itens = itens; }
}
