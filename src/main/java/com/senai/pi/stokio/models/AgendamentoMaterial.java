package com.senai.pi.stokio.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos_materiais")
public class AgendamentoMaterial {

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
    private Usuario usuario;

    @Column(name = "data_retirada")
    private LocalDateTime dataRetirada;

    @Column(name = "data_devolucao")
    private LocalDateTime dataDevolucao;

    @Column(name = "status")
    private String status = "PENDENTE"; // PENDENTE, APROVADO, CANCELADO

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    public AgendamentoMaterial() {}

    public AgendamentoMaterial(Materiais material, Categoria categoria, int quantidade, Usuario usuario, LocalDateTime dataRetirada, LocalDateTime dataDevolucao, String observacoes) {
        this.material = material;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.usuario = usuario;
        this.dataRetirada = dataRetirada;
        this.dataDevolucao = dataDevolucao;
        this.observacoes = observacoes;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Materiais getMaterial() { return material; }
    public void setMaterial(Materiais material) { this.material = material; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getDataRetirada() { return dataRetirada; }
    public void setDataRetirada(LocalDateTime dataRetirada) { this.dataRetirada = dataRetirada; }

    public LocalDateTime getDataDevolucao() { return dataDevolucao; }
    public void setDataDevolucao(LocalDateTime dataDevolucao) { this.dataDevolucao = dataDevolucao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
