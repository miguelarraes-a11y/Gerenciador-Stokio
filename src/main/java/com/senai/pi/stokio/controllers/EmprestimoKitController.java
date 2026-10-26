package com.senai.pi.stokio.controllers;

import com.senai.pi.stokio.models.EmprestimoKit;
import com.senai.pi.stokio.models.Kit;
import com.senai.pi.stokio.models.Usuario;
import com.senai.pi.stokio.repositories.EmprestimoKitRepository;
import com.senai.pi.stokio.repositories.KitRepository;
import com.senai.pi.stokio.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/emprestimos-kits")
@CrossOrigin(origins = "*")
public class EmprestimoKitController {

    @Autowired
    private EmprestimoKitRepository repo;

    @Autowired
    private KitRepository kitRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @GetMapping
    public List<EmprestimoKit> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmprestimoKit> buscar(@PathVariable Integer id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> solicitar(@RequestBody EmprestimoKit emp) {
        if (emp.getKit() == null || emp.getKit().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Kit não informado."));
        }
        Optional<Kit> kitOpt = kitRepo.findById(emp.getKit().getId());
        if (kitOpt.isEmpty() || "Excluído".equalsIgnoreCase(kitOpt.get().getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Kit inexistente ou indisponível."));
        }
        Kit kit = kitOpt.get();
        int qtd = emp.getQuantidade() > 0 ? emp.getQuantidade() : 1;
        emp.setQuantidade(qtd);

        if (kit.getQuantidadeDisponivel() < qtd) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Quantidade solicitada excede os exemplares disponíveis deste kit (" + kit.getQuantidadeDisponivel() + " disponíveis)."));
        }

        if (emp.getSolicitante() == null || emp.getSolicitante().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Identificação do solicitante é obrigatória."));
        }
        Optional<Usuario> solOpt = usuarioRepo.findById(emp.getSolicitante().getId());
        if (solOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Usuário solicitante não encontrado."));
        }
        Usuario solicitante = solOpt.get();
        emp.setSolicitante(solicitante);

        // Regra: Alunos DEVEM selecionar obrigatoriamente um professor responsável
        if (solicitante.isAluno()) {
            if (emp.getProfessorResponsavel() == null || emp.getProfessorResponsavel().getId() == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Para solicitações de alunos, a seleção de um Professor Responsável é obrigatória."));
            }
            Optional<Usuario> profOpt = usuarioRepo.findById(emp.getProfessorResponsavel().getId());
            if (profOpt.isEmpty() || !profOpt.get().isProfessor()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Professor responsável selecionado inválido ou inexistente."));
            }
            emp.setProfessorResponsavel(profOpt.get());
            emp.setStatus("Pendente");
        } else if (solicitante.isProfessor()) {
            // Professores solicitam empréstimo (vai para aprovação do Administrador)
            emp.setStatus("Pendente");
        } else if (solicitante.isAdmin()) {
            // Administradores têm aprovação direta
            emp.setStatus("Aprovado");
            emp.setAprovadoPor(solicitante);
            emp.setDataDecisao(LocalDateTime.now());
        } else {
            emp.setStatus("Pendente");
        }

        EmprestimoKit salvo = repo.save(emp);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<?> aprovar(
            @PathVariable Integer id,
            @RequestParam Integer usuarioId) {
        Optional<EmprestimoKit> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        EmprestimoKit emp = opt.get();

        Optional<Usuario> userOpt = usuarioRepo.findById(usuarioId);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Usuário não autenticado."));
        Usuario user = userOpt.get();

        // Validação de permissões:
        // - Admin pode aprovar qualquer solicitação.
        // - Professor pode aprovar apenas solicitações em que é o professorResponsavel, e NÃO for o solicitante.
        if (user.isAdmin()) {
            // autorizada
        } else if (user.isProfessor()) {
            if (emp.getSolicitante() != null && emp.getSolicitante().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Professores não podem aprovar as suas próprias solicitações."));
            }
            if (emp.getProfessorResponsavel() == null || !emp.getProfessorResponsavel().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Apenas o professor responsável designado pode aprovar esta solicitação."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Alunos não possuem permissão para aprovar empréstimos."));
        }

        emp.setStatus("Aprovado");
        emp.setAprovadoPor(user);
        emp.setDataDecisao(LocalDateTime.now());
        return ResponseEntity.ok(repo.save(emp));
    }

    @PutMapping("/{id}/recusar")
    public ResponseEntity<?> recusar(
            @PathVariable Integer id,
            @RequestParam Integer usuarioId,
            @RequestParam(required = false) String motivo) {
        Optional<EmprestimoKit> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        EmprestimoKit emp = opt.get();

        Optional<Usuario> userOpt = usuarioRepo.findById(usuarioId);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Usuário não autenticado."));
        Usuario user = userOpt.get();

        if (user.isAdmin()) {
            // autorizada
        } else if (user.isProfessor()) {
            if (emp.getSolicitante() != null && emp.getSolicitante().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Professores não podem recusar suas próprias solicitações."));
            }
            if (emp.getProfessorResponsavel() == null || !emp.getProfessorResponsavel().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Apenas o professor responsável designado pode recusar esta solicitação."));
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Alunos não possuem permissão para recusar empréstimos."));
        }

        emp.setStatus("Recusado");
        emp.setAprovadoPor(user);
        emp.setDataDecisao(LocalDateTime.now());
        if (motivo != null && !motivo.trim().isEmpty()) {
            emp.setMotivoRecusa(motivo.trim());
        }
        return ResponseEntity.ok(repo.save(emp));
    }

    @PutMapping("/{id}/retirar")
    public ResponseEntity<?> retirar(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer usuarioId) {
        Optional<EmprestimoKit> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        EmprestimoKit emp = opt.get();

        if (!"Aprovado".equalsIgnoreCase(emp.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Apenas empréstimos aprovados podem ser retirados."));
        }

        if (emp.getKit() != null) {
            Optional<Kit> kitOpt = kitRepo.findById(emp.getKit().getId());
            if (kitOpt.isPresent()) {
                Kit k = kitOpt.get();
                int novaQtd = Math.max(0, k.getQuantidadeDisponivel() - emp.getQuantidade());
                k.setQuantidadeDisponivel(novaQtd);
                if (novaQtd == 0) k.setStatus("Em Uso");
                kitRepo.save(k);
            }
        }

        emp.setStatus("Retirado");
        emp.setDataRetirada(LocalDateTime.now());
        return ResponseEntity.ok(repo.save(emp));
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<?> devolver(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer usuarioId) {
        Optional<EmprestimoKit> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        EmprestimoKit emp = opt.get();

        if (!"Retirado".equalsIgnoreCase(emp.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Apenas empréstimos retirados podem ser devolvidos."));
        }

        if (emp.getKit() != null) {
            Optional<Kit> kitOpt = kitRepo.findById(emp.getKit().getId());
            if (kitOpt.isPresent()) {
                Kit k = kitOpt.get();
                k.setQuantidadeDisponivel(k.getQuantidadeDisponivel() + emp.getQuantidade());
                k.setStatus("Disponível");
                kitRepo.save(k);
            }
        }

        emp.setStatus("Devolvido");
        emp.setDataDevolucaoReal(LocalDateTime.now());
        return ResponseEntity.ok(repo.save(emp));
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
