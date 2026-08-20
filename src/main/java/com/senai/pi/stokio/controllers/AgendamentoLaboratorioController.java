package com.senai.pi.stokio.controllers;

import com.senai.pi.stokio.models.AgendamentoLaboratorio;
import com.senai.pi.stokio.repositories.AgendamentoLaboratorioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/agendamentos-laboratorio")
@CrossOrigin(origins = "*")
public class AgendamentoLaboratorioController {

    @Autowired
    private AgendamentoLaboratorioRepository repo;

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
        if (ag.getStatus() == null) ag.setStatus("PENDENTE");
        if (ag.getHoraInicio() == null) ag.setHoraInicio(LocalTime.of(8, 0));
        if (ag.getHoraFim() == null) ag.setHoraFim(LocalTime.of(12, 0));

        // Time conflict validation: Check if another active booking exists on the same space and date with overlapping time
        List<AgendamentoLaboratorio> existing = repo.findAll();
        boolean hasConflict = existing.stream()
                .filter(e -> !"CANCELADO".equalsIgnoreCase(e.getStatus()))
                .filter(e -> e.getLaboratorio() != null && e.getLaboratorio().equalsIgnoreCase(ag.getLaboratorio()))
                .filter(e -> e.getData() != null && e.getData().equals(ag.getData()))
                .anyMatch(e -> {
                    LocalTime eStart = e.getHoraInicio() != null ? e.getHoraInicio() : LocalTime.of(8, 0);
                    LocalTime eEnd = e.getHoraFim() != null ? e.getHoraFim() : LocalTime.of(12, 0);
                    LocalTime nStart = ag.getHoraInicio();
                    LocalTime nEnd = ag.getHoraFim();
                    // Overlap condition: (nStart < eEnd) && (nEnd > eStart)
                    return nStart.isBefore(eEnd) && nEnd.isAfter(eStart);
                });

        if (hasConflict) {
            return ResponseEntity.badRequest().body("{\"error\": \"Conflito de horário: Já existe um agendamento para este espaço no período solicitado.\"}");
        }

        AgendamentoLaboratorio salvo = repo.save(ag);
        return ResponseEntity.ok(salvo);
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
