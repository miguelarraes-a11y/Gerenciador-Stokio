package com.senai.pi.stokio.controllers;

import com.senai.pi.stokio.models.EmprestimoKit;
import com.senai.pi.stokio.models.Kit;
import com.senai.pi.stokio.repositories.EmprestimoKitRepository;
import com.senai.pi.stokio.repositories.KitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/emprestimos-kits")
@CrossOrigin(origins = "*")
public class EmprestimoKitController {

    @Autowired
    private EmprestimoKitRepository repo;

    @Autowired
    private KitRepository kitRepo;

    @GetMapping
    public List<EmprestimoKit> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmprestimoKit> buscar(@PathVariable Integer id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EmprestimoKit> solicitar(@RequestBody EmprestimoKit emp) {
        if (emp.getStatus() == null) emp.setStatus("Pendente");
        EmprestimoKit salvo = repo.save(emp);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<EmprestimoKit> atualizarStatus(@PathVariable Integer id, @RequestParam String status) {
        Optional<EmprestimoKit> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        EmprestimoKit emp = opt.get();
        String statusAnterior = emp.getStatus();
        emp.setStatus(status);

        if (emp.getKit() != null) {
            Optional<Kit> kitOpt = kitRepo.findById(emp.getKit().getId());
            if (kitOpt.isPresent()) {
                Kit k = kitOpt.get();
                if ("Retirado".equalsIgnoreCase(status) && !"Retirado".equalsIgnoreCase(statusAnterior)) {
                    int novaQtd = Math.max(0, k.getQuantidadeDisponivel() - emp.getQuantidade());
                    k.setQuantidadeDisponivel(novaQtd);
                    if (novaQtd == 0) k.setStatus("Em Uso");
                    kitRepo.save(k);
                    emp.setDataRetirada(LocalDateTime.now());
                }
                if ("Devolvido".equalsIgnoreCase(status) && "Retirado".equalsIgnoreCase(statusAnterior)) {
                    k.setQuantidadeDisponivel(k.getQuantidadeDisponivel() + emp.getQuantidade());
                    k.setStatus("Disponível");
                    kitRepo.save(k);
                    emp.setDataDevolucaoReal(LocalDateTime.now());
                }
            }
        }

        EmprestimoKit atualizado = repo.save(emp);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
