package com.senai.pi.stokio.models;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    @Column(name = "quantidade", nullable = false)
    private int quantidade;

    @Column(name = "status", nullable = false)
    private String status; // PENDENTE, CONFIRMADO, CONCLUIDO, CANCELADO

    @Column(name = "observacoes")
    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Materiais material;

    public Agendamento() {
    }

    public Agendamento(Integer id, String descricao, LocalDateTime dataInicio, LocalDateTime dataFim, 
                       int quantidade, String status, String observacoes, Usuario usuario, Materiais material) {
        this.id = id;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.quantidade = quantidade;
        this.status = status;
        this.observacoes = observacoes;
        this.usuario = usuario;
        this.material = material;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Materiais getMaterial() {
        return material;
    }

    public void setMaterial(Materiais material) {
        this.material = material;
    }
}
