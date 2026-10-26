package com.senai.pi.stokio.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="nome")
    private String nome;

    @Column(name="email")
    private String email;

    @Column(name="senha")
    private String senha;

    @Column(name="tipo_usuario")
    private String tipoUsuario;

    @Column(name="uf")
    private String uf;

    @Column(name="foto_url", length=1000)
    private String fotoUrl;

    @Column(name="telefone")
    private String telefone;

    public Usuario() {
    }

    public Usuario(Integer id, String nome, String email, String senha, String tipoUsuario, String uf) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
        this.uf = uf;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public boolean isAdmin() {
        return tipoUsuario != null && (tipoUsuario.equalsIgnoreCase("admin") || tipoUsuario.equalsIgnoreCase("administrador"));
    }

    public boolean isProfessor() {
        return tipoUsuario != null && (tipoUsuario.equalsIgnoreCase("professor") || tipoUsuario.equalsIgnoreCase("docente"));
    }

    public boolean isAluno() {
        return tipoUsuario != null && (tipoUsuario.equalsIgnoreCase("aluno") || tipoUsuario.equalsIgnoreCase("estudante"));
    }
}