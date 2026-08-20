package com.senai.pi.stokio.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "kit_itens")
public class KitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "kit_id", nullable = false)
    @JsonIgnore
    private Kit kit;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Materiais material;

    @Column(name = "quantidade", nullable = false)
    private int quantidade;

    public KitItem() {}

    public KitItem(Kit kit, Materiais material, int quantidade) {
        this.kit = kit;
        this.material = material;
        this.quantidade = quantidade;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Kit getKit() { return kit; }
    public void setKit(Kit kit) { this.kit = kit; }

    public Materiais getMaterial() { return material; }
    public void setMaterial(Materiais material) { this.material = material; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
}
