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
import jakarta.persistence.Table;

@Entity
@Table(name="danos")
public class Danos{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="descricao_dano")
    private String descricaoDano;

    @Column(name="status_resolucao")
    private String statusResolucao;

    @Column(name="data_registro")
    private LocalDateTime dataRegistro;

    @ManyToMany
     @JoinTable(
        name = "danos_materiais",
        joinColumns = @JoinColumn(name = "danos_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "materiais_id", referencedColumnName = "id")
    )
    private Set<Materiais> materiais;

    public Danos() {
    }

    public Danos(Integer id, String descricaoDano, String statusResolucao, LocalDateTime dataRegistro,
            Set<Materiais> materiais) {
        this.id = id;
        this.descricaoDano = descricaoDano;
        this.statusResolucao = statusResolucao;
        this.dataRegistro = dataRegistro;
        this.materiais = materiais;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricaoDano() {
        return descricaoDano;
    }

    public void setDescricaoDano(String descricaoDano) {
        this.descricaoDano = descricaoDano;
    }

    public String getStatusResolucao() {
        return statusResolucao;
    }

    public void setStatusResolucao(String statusResolucao) {
        this.statusResolucao = statusResolucao;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public Set<Materiais> getMateriais() {
        return materiais;
    }

    public void setMateriais(Set<Materiais> materiais) {
        this.materiais = materiais;
    }

} 