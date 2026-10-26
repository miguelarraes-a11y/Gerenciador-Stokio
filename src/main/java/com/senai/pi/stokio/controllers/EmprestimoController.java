package com.senai.pi.stokio.controllers;

import com.senai.pi.stokio.models.Emprestimo;
import com.senai.pi.stokio.models.Materiais;
import com.senai.pi.stokio.models.Usuario;
import com.senai.pi.stokio.repositories.EmprestimoRepository;
import com.senai.pi.stokio.repositories.MateriaisRepository;
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
@RequestMapping("/emprestimos")
@CrossOrigin(origins = "*")
public class EmprestimoController {

    @Autowired
    private EmprestimoRepository repo;

    @Autowired
    private MateriaisRepository materiaisRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @GetMapping
    public List<Emprestimo> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Emprestimo> buscar(@PathVariable Integer id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> solicitar(@RequestBody Emprestimo emp) {
        if (emp.getMaterial() == null || emp.getMaterial().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Material não informado."));
        }
        Optional<Materiais> matOpt = materiaisRepo.findById(emp.getMaterial().getId());
        if (matOpt.isEmpty() || "Excluído".equalsIgnoreCase(matOpt.get().getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Material inexistente ou indisponível."));
        }
        Materiais mat = matOpt.get();
        int qtd = emp.getQuantidade() > 0 ? emp.getQuantidade() : 1;
        emp.setQuantidade(qtd);

        if (mat.getQuantidadeDisponivel() < qtd) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Quantidade solicitada excede o saldo em estoque (" + mat.getQuantidadeDisponivel() + " disponíveis)."));
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

        // Alunos têm acesso apenas a empréstimo de kits pedagógicos e reservas de laboratório
        if (solicitante.isAluno()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", "Alunos possuem acesso apenas para empréstimo de kits pedagógicos e agendamentos de espaços."));
        }

        if (solicitante.isAdmin()) {
            emp.setStatus("Aprovado");
            emp.setAprovadoPor(solicitante);
            emp.setDataDecisao(LocalDateTime.now());
        } else {
            // Professores solicitam empréstimo (aguarda aprovação do Administrador)
            emp.setStatus("Pendente");
        }

        if (emp.getCategoria() == null && mat.getCategorias() != null) {
            emp.setCategoria(mat.getCategorias());
        }

        Emprestimo salvo = repo.save(emp);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<?> aprovar(
            @PathVariable Integer id,
            @RequestParam Integer usuarioId) {
        Optional<Emprestimo> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Emprestimo emp = opt.get();

        Optional<Usuario> userOpt = usuarioRepo.findById(usuarioId);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Usuário não autenticado."));
        Usuario user = userOpt.get();

        if (!user.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Apenas administradores podem aprovar solicitações de empréstimo de materiais."));
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
        Optional<Emprestimo> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Emprestimo emp = opt.get();

        Optional<Usuario> userOpt = usuarioRepo.findById(usuarioId);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Usuário não autenticado."));
        Usuario user = userOpt.get();

        if (!user.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Apenas administradores podem recusar solicitações de empréstimo de materiais."));
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
        Optional<Emprestimo> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Emprestimo emp = opt.get();

        if (!"Aprovado".equalsIgnoreCase(emp.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Apenas empréstimos aprovados podem ser retirados."));
        }

        if (emp.getMaterial() != null) {
            Optional<Materiais> matOpt = materiaisRepo.findById(emp.getMaterial().getId());
            if (matOpt.isPresent()) {
                Materiais mat = matOpt.get();
                int novaDisp = Math.max(0, mat.getQuantidadeDisponivel() - emp.getQuantidade());
                mat.setQuantidadeDisponivel(novaDisp);
                mat.setQuantidadeEmprestada(mat.getQuantidadeEmprestada() + emp.getQuantidade());
                if (novaDisp == 0) mat.setStatus("Indisponível");
                materiaisRepo.save(mat);
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
        Optional<Emprestimo> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Emprestimo emp = opt.get();

        if (!"Retirado".equalsIgnoreCase(emp.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Apenas empréstimos com status Retirado podem ser devolvidos."));
        }

        if (emp.getMaterial() != null) {
            Optional<Materiais> matOpt = materiaisRepo.findById(emp.getMaterial().getId());
            if (matOpt.isPresent()) {
                Materiais mat = matOpt.get();
                mat.setQuantidadeDisponivel(mat.getQuantidadeDisponivel() + emp.getQuantidade());
                mat.setQuantidadeEmprestada(Math.max(0, mat.getQuantidadeEmprestada() - emp.getQuantidade()));
                mat.setStatus("Disponível");
                materiaisRepo.save(mat);
            }
        }

        emp.setStatus("Devolvido");
        emp.setDataDevolucaoReal(LocalDateTime.now());
        return ResponseEntity.ok(repo.save(emp));
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
