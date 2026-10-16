package com.senai.pi.stokio.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "solicitacoes_kit_itens")
public class SolicitacaoKitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "solicitacao_id", nullable = false)
    @JsonIgnore
    private SolicitacaoKit solicitacaoKit;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Materiais material;

    @Column(name = "quantidade", nullable = false)
    private int quantidade;

    public SolicitacaoKitItem() {}

    public SolicitacaoKitItem(SolicitacaoKit solicitacaoKit, Materiais material, int quantidade) {
        this.solicitacaoKit = solicitacaoKit;
        this.material = material;
        this.quantidade = quantidade;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public SolicitacaoKit getSolicitacaoKit() { return solicitacaoKit; }
    public void setSolicitacaoKit(SolicitacaoKit solicitacaoKit) { this.solicitacaoKit = solicitacaoKit; }

    public Materiais getMaterial() { return material; }
    public void setMaterial(Materiais material) { this.material = material; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
}
