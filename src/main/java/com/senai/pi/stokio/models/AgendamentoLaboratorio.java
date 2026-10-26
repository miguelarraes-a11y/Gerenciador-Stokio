package com.senai.pi.stokio.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "agendamentos_laboratorio")
public class AgendamentoLaboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "laboratorio", nullable = false)
    private String laboratorio; // Fab Lab, Sala de Prototipagem, Sala 3D, Sala Eletrônica, Corte Laser

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "data_reserva")
    private LocalDate data;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    @Column(name = "status")
    private String status = "PENDENTE"; // PENDENTE, APROVADO, RECUSADO, CANCELADO

    @ManyToOne
    @JoinColumn(name = "professor_responsavel_id")
    private Usuario professorResponsavel;

    @ManyToOne
    @JoinColumn(name = "aprovado_por_id")
    private Usuario aprovadoPor;

    @Column(name = "data_decisao")
    private LocalDateTime dataDecisao;

    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
        if (this.status == null) this.status = "PENDENTE";
    }

    public AgendamentoLaboratorio() {}

    public AgendamentoLaboratorio(String laboratorio, Usuario usuario, LocalDate data, LocalTime horaInicio, LocalTime horaFim, String observacoes) {
        this.laboratorio = laboratorio;
        this.usuario = usuario;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.observacoes = observacoes;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLaboratorio() { return laboratorio; }
    public void setLaboratorio(String laboratorio) { this.laboratorio = laboratorio; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public Usuario getProfessorResponsavel() { return professorResponsavel; }
    public void setProfessorResponsavel(Usuario professorResponsavel) { this.professorResponsavel = professorResponsavel; }

    public Usuario getAprovadoPor() { return aprovadoPor; }
    public void setAprovadoPor(Usuario aprovadoPor) { this.aprovadoPor = aprovadoPor; }

    public LocalDateTime getDataDecisao() { return dataDecisao; }
    public void setDataDecisao(LocalDateTime dataDecisao) { this.dataDecisao = dataDecisao; }

    public String getMotivoRecusa() { return motivoRecusa; }
    public void setMotivoRecusa(String motivoRecusa) { this.motivoRecusa = motivoRecusa; }
}
