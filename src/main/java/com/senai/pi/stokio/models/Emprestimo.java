package com.senai.pi.stokio.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emprestimos")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Materiais material;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "quantidade", nullable = false)
    private int quantidade = 1;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario solicitante;

    @Column(name = "data_retirada")
    private LocalDateTime dataRetirada;

    @Column(name = "data_devolucao_prevista")
    private LocalDateTime dataDevolucao;

    @Column(name = "data_devolucao_real")
    private LocalDateTime dataDevolucaoReal;

    @Column(name = "status")
    private String status = "Pendente"; // Pendente, Aprovado, Retirado, Devolvido, Atrasado, Cancelado

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    public Emprestimo() {}

    public Emprestimo(Materiais material, Categoria categoria, int quantidade, Usuario solicitante, LocalDateTime dataRetirada, LocalDateTime dataDevolucao, String observacoes) {
        this.material = material;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.solicitante = solicitante;
        this.dataRetirada = dataRetirada;
        this.dataDevolucao = dataDevolucao;
        this.observacoes = observacoes;
        this.status = "Pendente";
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Materiais getMaterial() { return material; }
    public void setMaterial(Materiais material) { this.material = material; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public Usuario getSolicitante() { return solicitante; }
    public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }

    public LocalDateTime getDataRetirada() { return dataRetirada; }
    public void setDataRetirada(LocalDateTime dataRetirada) { this.dataRetirada = dataRetirada; }

    public LocalDateTime getDataDevolucao() { return dataDevolucao; }
    public void setDataDevolucao(LocalDateTime dataDevolucao) { this.dataDevolucao = dataDevolucao; }

    public LocalDateTime getDataDevolucaoReal() { return dataDevolucaoReal; }
    public void setDataDevolucaoReal(LocalDateTime dataDevolucaoReal) { this.dataDevolucaoReal = dataDevolucaoReal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
