package com.senai.pi.stokio.controllers;

import com.senai.pi.stokio.models.SolicitacaoKit;
import com.senai.pi.stokio.services.SolicitacaoKitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/solicitacoes-kits")
@CrossOrigin(origins = "*")
public class SolicitacaoKitController {

    @Autowired
    private SolicitacaoKitService solicitacaoKitService;

    @GetMapping
    public List<SolicitacaoKit> listar(@RequestParam(required = false) Integer solicitanteId) {
        if (solicitanteId != null) {
            return solicitacaoKitService.listarPorSolicitante(solicitanteId);
        }
        return solicitacaoKitService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        return solicitacaoKitService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> payload) {
        try {
            SolicitacaoKit criada = solicitacaoKitService.criar(payload);
            return ResponseEntity.status(HttpStatus.CREATED).body(criada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro interno ao processar solicitação: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<?> aprovar(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer adminId,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            Integer finalAdminId = adminId;
            if (finalAdminId == null && body != null && body.containsKey("adminId")) {
                Object obj = body.get("adminId");
                finalAdminId = obj instanceof Integer ? (Integer) obj : Integer.parseInt(obj.toString());
            }

            SolicitacaoKit aprovada = solicitacaoKitService.aprovar(id, finalAdminId);
            return ResponseEntity.ok(aprovada);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao aprovar solicitação: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/recusar")
    public ResponseEntity<?> recusar(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer adminId,
            @RequestParam(required = false) String justificativa,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            Integer finalAdminId = adminId;
            String finalJustificativa = justificativa;

            if (body != null) {
                if (finalAdminId == null && body.containsKey("adminId")) {
                    Object obj = body.get("adminId");
                    finalAdminId = obj instanceof Integer ? (Integer) obj : Integer.parseInt(obj.toString());
                }
                if (finalJustificativa == null && body.containsKey("justificativa")) {
                    finalJustificativa = (String) body.get("justificativa");
                }
            }

            SolicitacaoKit recusada = solicitacaoKitService.recusar(id, finalAdminId, finalJustificativa);
            return ResponseEntity.ok(recusada);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao recusar solicitação: " + e.getMessage()));
        }
    }
}
