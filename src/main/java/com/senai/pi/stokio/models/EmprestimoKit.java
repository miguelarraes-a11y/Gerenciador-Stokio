package com.senai.pi.stokio.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emprestimos_kits")
public class EmprestimoKit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "kit_id", nullable = false)
    private Kit kit;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario solicitante;

    @Column(name = "quantidade")
    private int quantidade = 1;

    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_retirada")
    private LocalDateTime dataRetirada;

    @Column(name = "data_devolucao_prevista")
    private LocalDateTime dataDevolucaoPrevista;

    @Column(name = "data_devolucao_real")
    private LocalDateTime dataDevolucaoReal;

    @Column(name = "status")
    private String status = "Pendente"; // Pendente, Aprovado, Retirado, Devolvido, Atrasado, Cancelado

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    @PrePersist
    protected void onCreate() {
        this.dataSolicitacao = LocalDateTime.now();
        if (this.status == null) this.status = "Pendente";
        if (this.dataDevolucaoPrevista == null) this.dataDevolucaoPrevista = LocalDateTime.now().plusDays(3);
    }

    public EmprestimoKit() {}

    public EmprestimoKit(Kit kit, Usuario solicitante, int quantidade, LocalDateTime dataRetirada, LocalDateTime dataDevolucaoPrevista, String observacoes) {
        this.kit = kit;
        this.solicitante = solicitante;
        this.quantidade = quantidade;
        this.dataRetirada = dataRetirada;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.observacoes = observacoes;
        this.status = "Pendente";
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Kit getKit() { return kit; }
    public void setKit(Kit kit) { this.kit = kit; }

    public Usuario getSolicitante() { return solicitante; }
    public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }

    public LocalDateTime getDataRetirada() { return dataRetirada; }
    public void setDataRetirada(LocalDateTime dataRetirada) { this.dataRetirada = dataRetirada; }

    public LocalDateTime getDataDevolucaoPrevista() { return dataDevolucaoPrevista; }
    public void setDataDevolucaoPrevista(LocalDateTime dataDevolucaoPrevista) { this.dataDevolucaoPrevista = dataDevolucaoPrevista; }

    public LocalDateTime getDataDevolucaoReal() { return dataDevolucaoReal; }
    public void setDataDevolucaoReal(LocalDateTime dataDevolucaoReal) { this.dataDevolucaoReal = dataDevolucaoReal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
