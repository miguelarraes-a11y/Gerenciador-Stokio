package com.senai.pi.stokio.controllers;

import com.senai.pi.stokio.models.AgendamentoLaboratorio;
import com.senai.pi.stokio.models.Usuario;
import com.senai.pi.stokio.repositories.AgendamentoLaboratorioRepository;
import com.senai.pi.stokio.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/agendamentos-laboratorio")
@CrossOrigin(origins = "*")
public class AgendamentoLaboratorioController {

    @Autowired
    private AgendamentoLaboratorioRepository repo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @GetMapping
    public List<AgendamentoLaboratorio> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoLaboratorio> buscar(@PathVariable Integer id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody AgendamentoLaboratorio ag) {
        if (ag.getLaboratorio() == null || ag.getLaboratorio().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Espaço de laboratório é obrigatório."));
        }
        if (ag.getData() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Data do agendamento é obrigatória."));
        }
        if (ag.getHoraInicio() == null) ag.setHoraInicio(LocalTime.of(8, 0));
        if (ag.getHoraFim() == null) ag.setHoraFim(LocalTime.of(12, 0));

        if (ag.getUsuario() == null || ag.getUsuario().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Identificação do solicitante é obrigatória."));
        }
        Optional<Usuario> solOpt = usuarioRepo.findById(ag.getUsuario().getId());
        if (solOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Usuário solicitante não encontrado."));
        }
        Usuario solicitante = solOpt.get();
        ag.setUsuario(solicitante);

        // Regra: Aluno deve obrigatoriamente selecionar um professor responsável
        if (solicitante.isAluno()) {
            if (ag.getProfessorResponsavel() == null || ag.getProfessorResponsavel().getId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Para agendamentos de alunos, a seleção de um Professor Responsável é obrigatória."));
            }
            Optional<Usuario> profOpt = usuarioRepo.findById(ag.getProfessorResponsavel().getId());
            if (profOpt.isEmpty() || !profOpt.get().isProfessor()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Professor responsável selecionado inválido ou inexistente."));
            }
            ag.setProfessorResponsavel(profOpt.get());
            ag.setStatus("PENDENTE");
        } else if (solicitante.isProfessor()) {
            ag.setStatus("PENDENTE");
        } else if (solicitante.isAdmin()) {
            ag.setStatus("APROVADO");
            ag.setAprovadoPor(solicitante);
            ag.setDataDecisao(LocalDateTime.now());
        } else {
            ag.setStatus("PENDENTE");
        }

        // Validação de conflito de horário no mesmo espaço e data
        List<AgendamentoLaboratorio> existing = repo.findAll();
        boolean hasConflict = existing.stream()
                .filter(e -> !"CANCELADO".equalsIgnoreCase(e.getStatus()) && !"RECUSADO".equalsIgnoreCase(e.getStatus()))
                .filter(e -> e.getLaboratorio() != null && e.getLaboratorio().equalsIgnoreCase(ag.getLaboratorio()))
                .filter(e -> e.getData() != null && e.getData().equals(ag.getData()))
                .anyMatch(e -> {
                    LocalTime eStart = e.getHoraInicio() != null ? e.getHoraInicio() : LocalTime.of(8, 0);
                    LocalTime eEnd = e.getHoraFim() != null ? e.getHoraFim() : LocalTime.of(12, 0);
                    LocalTime nStart = ag.getHoraInicio();
                    LocalTime nEnd = ag.getHoraFim();
                    return nStart.isBefore(eEnd) && nEnd.isAfter(eStart);
                });

        if (hasConflict) {
            return ResponseEntity.badRequest().body(Map.of("error", "Conflito de horário: Já existe um agendamento para este espaço no período solicitado."));
        }

        AgendamentoLaboratorio salvo = repo.save(ag);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<?> aprovar(
            @PathVariable Integer id,
            @RequestParam Integer usuarioId) {
        Optional<AgendamentoLaboratorio> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        AgendamentoLaboratorio ag = opt.get();

        Optional<Usuario> userOpt = usuarioRepo.findById(usuarioId);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuário não autenticado."));
        Usuario user = userOpt.get();

        if (user.isAdmin()) {
            // autorizado
        } else if (user.isProfessor()) {
            if (ag.getUsuario() != null && ag.getUsuario().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Professores não podem aprovar seus próprios agendamentos."));
            }
            if (ag.getProfessorResponsavel() == null || !ag.getProfessorResponsavel().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Apenas o professor responsável designado pode aprovar este agendamento."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Alunos não possuem permissão para aprovar agendamentos."));
        }

        ag.setStatus("APROVADO");
        ag.setAprovadoPor(user);
        ag.setDataDecisao(LocalDateTime.now());
        return ResponseEntity.ok(repo.save(ag));
    }

    @PutMapping("/{id}/recusar")
    public ResponseEntity<?> recusar(
            @PathVariable Integer id,
            @RequestParam Integer usuarioId,
            @RequestParam(required = false) String motivo) {
        Optional<AgendamentoLaboratorio> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        AgendamentoLaboratorio ag = opt.get();

        Optional<Usuario> userOpt = usuarioRepo.findById(usuarioId);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuário não autenticado."));
        Usuario user = userOpt.get();

        if (user.isAdmin()) {
            // autorizado
        } else if (user.isProfessor()) {
            if (ag.getUsuario() != null && ag.getUsuario().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Professores não podem recusar seus próprios agendamentos."));
            }
            if (ag.getProfessorResponsavel() == null || !ag.getProfessorResponsavel().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Apenas o professor responsável designado pode recusar este agendamento."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Alunos não possuem permissão para recusar agendamentos."));
        }

        ag.setStatus("RECUSADO");
        ag.setAprovadoPor(user);
        ag.setDataDecisao(LocalDateTime.now());
        if (motivo != null && !motivo.trim().isEmpty()) {
            ag.setMotivoRecusa(motivo.trim());
        }
        return ResponseEntity.ok(repo.save(ag));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AgendamentoLaboratorio> atualizarStatus(@PathVariable Integer id, @RequestParam String status) {
        return repo.findById(id).map(ag -> {
            ag.setStatus(status);
            return ResponseEntity.ok(repo.save(ag));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
