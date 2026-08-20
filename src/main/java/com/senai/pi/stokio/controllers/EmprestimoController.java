package com.senai.pi.stokio.controllers;

import com.senai.pi.stokio.models.Emprestimo;
import com.senai.pi.stokio.models.Materiais;
import com.senai.pi.stokio.repositories.EmprestimoRepository;
import com.senai.pi.stokio.repositories.MateriaisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/emprestimos")
@CrossOrigin(origins = "*")
public class EmprestimoController {

    @Autowired
    private EmprestimoRepository repo;

    @Autowired
    private MateriaisRepository materiaisRepo;

    @GetMapping
    public List<Emprestimo> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Emprestimo> buscar(@PathVariable Integer id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Emprestimo> solicitar(@RequestBody Emprestimo emp) {
        if (emp.getStatus() == null) emp.setStatus("Pendente");
        Emprestimo salvo = repo.save(emp);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Emprestimo> atualizarStatus(@PathVariable Integer id, @RequestParam String status) {
        Optional<Emprestimo> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Emprestimo emp = opt.get();
        String statusAnterior = emp.getStatus();
        emp.setStatus(status);

        // Inventory Stock Integration Logic
        if (emp.getMaterial() != null) {
            Optional<Materiais> matOpt = materiaisRepo.findById(emp.getMaterial().getId());
            if (matOpt.isPresent()) {
                Materiais mat = matOpt.get();

                // When status moves to "Retirado", decrease available stock and increase borrowed stock
                if ("Retirado".equalsIgnoreCase(status) && !"Retirado".equalsIgnoreCase(statusAnterior)) {
                    int novaDisp = Math.max(0, mat.getQuantidadeDisponivel() - emp.getQuantidade());
                    mat.setQuantidadeDisponivel(novaDisp);
                    mat.setQuantidadeEmprestada(mat.getQuantidadeEmprestada() + emp.getQuantidade());
                    if (novaDisp == 0) mat.setStatus("Indisponível");
                    materiaisRepo.save(mat);
                    emp.setDataRetirada(LocalDateTime.now());
                }

                // When status moves to "Devolvido", increase available stock and decrease borrowed stock
                if ("Devolvido".equalsIgnoreCase(status) && "Retirado".equalsIgnoreCase(statusAnterior)) {
                    mat.setQuantidadeDisponivel(mat.getQuantidadeDisponivel() + emp.getQuantidade());
                    mat.setQuantidadeEmprestada(Math.max(0, mat.getQuantidadeEmprestada() - emp.getQuantidade()));
                    mat.setStatus("Disponível");
                    materiaisRepo.save(mat);
                    emp.setDataDevolucaoReal(LocalDateTime.now());
                }
            }
        }

        Emprestimo atualizado = repo.save(emp);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
