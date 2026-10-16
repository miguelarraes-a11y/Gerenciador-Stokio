package com.senai.pi.stokio.services;

import com.senai.pi.stokio.models.*;
import com.senai.pi.stokio.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SolicitacaoKitService {

    @Autowired
    private SolicitacaoKitRepository solicitacaoKitRepository;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private MateriaisRepository materiaisRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<SolicitacaoKit> listarTodas() {
        return solicitacaoKitRepository.findAllByOrderByDataSolicitacaoDesc();
    }

    public List<SolicitacaoKit> listarPorSolicitante(Integer solicitanteId) {
        return solicitacaoKitRepository.findBySolicitanteIdOrderByDataSolicitacaoDesc(solicitanteId);
    }

    public Optional<SolicitacaoKit> buscarPorId(Integer id) {
        return solicitacaoKitRepository.findById(id);
    }

    @Transactional
    public SolicitacaoKit criar(Map<String, Object> payload) {
        String nome = (String) payload.get("nome");
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do kit é obrigatório.");
        }
        nome = nome.trim();

        // Evitar duplicidade com kits ativos já cadastrados
        if (kitRepository.existsByNomeIgnoreCaseAndStatusNot(nome, "Excluído")) {
            throw new IllegalArgumentException("Já existe um kit cadastrado com o nome '" + nome + "'.");
        }

        // Evitar duplicidade com solicitações pendentes
        if (solicitacaoKitRepository.existsByNomeIgnoreCaseAndStatus(nome, "Pendente")) {
            throw new IllegalArgumentException("Já existe uma solicitação pendente para um kit com o nome '" + nome + "'.");
        }

        Object solicitanteIdObj = payload.get("solicitanteId");
        if (solicitanteIdObj == null) {
            throw new IllegalArgumentException("Identificação do solicitante é obrigatória.");
        }
        int solicitanteId = solicitanteIdObj instanceof Integer ? (Integer) solicitanteIdObj : Integer.parseInt(solicitanteIdObj.toString());
        Usuario solicitante = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário solicitante não encontrado."));

        SolicitacaoKit sol = new SolicitacaoKit();
        sol.setNome(nome);
        sol.setDescricao((String) payload.getOrDefault("descricao", ""));
        sol.setFinalidade((String) payload.getOrDefault("finalidade", ""));
        sol.setSolicitante(solicitante);
        sol.setStatus("Pendente");
        sol.setDataSolicitacao(LocalDateTime.now());

        if (payload.containsKey("quantidadeDisponivel")) {
            Object q = payload.get("quantidadeDisponivel");
            sol.setQuantidadeDisponivel(q instanceof Integer ? (Integer) q : Integer.parseInt(q.toString()));
        }

        if (payload.containsKey("categoriaId") && payload.get("categoriaId") != null) {
            Integer catId = (Integer) payload.get("categoriaId");
            Optional<Categoria> cat = categoriaRepository.findById(catId);
            cat.ifPresent(sol::setCategoria);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itens = (List<Map<String, Object>>) payload.get("itens");
        if (itens != null) {
            for (Map<String, Object> itemData : itens) {
                Object matIdObj = itemData.get("materialId");
                if (matIdObj == null) continue;
                int materialId = matIdObj instanceof Integer ? (Integer) matIdObj : Integer.parseInt(matIdObj.toString());
                
                Object qtdObj = itemData.getOrDefault("quantidade", 1);
                int qtd = qtdObj instanceof Integer ? (Integer) qtdObj : Integer.parseInt(qtdObj.toString());

                Optional<Materiais> mat = materiaisRepository.findById(materialId);
                if (mat.isPresent()) {
                    SolicitacaoKitItem ski = new SolicitacaoKitItem(sol, mat.get(), qtd);
                    sol.getItens().add(ski);
                }
            }
        }

        return solicitacaoKitRepository.save(sol);
    }

    private Usuario validarAdministrador(Integer adminId) {
        if (adminId == null) {
            throw new SecurityException("Identificação do usuário administrador é obrigatória.");
        }
        Usuario user = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        String tipo = user.getTipoUsuario() != null ? user.getTipoUsuario().toLowerCase() : "";
        if (!tipo.contains("admin")) {
            throw new SecurityException("Apenas administradores têm permissão para aprovar ou recusar solicitações.");
        }
        return user;
    }

    @Transactional
    public SolicitacaoKit aprovar(Integer solicitacaoId, Integer adminId) {
        Usuario admin = validarAdministrador(adminId);

        SolicitacaoKit sol = solicitacaoKitRepository.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada."));

        if (!"Pendente".equalsIgnoreCase(sol.getStatus())) {
            throw new IllegalStateException("Esta solicitação já foi finalizada com status: " + sol.getStatus());
        }

        // Validação adicional contra criação de kits duplicados
        if (kitRepository.existsByNomeIgnoreCaseAndStatusNot(sol.getNome(), "Excluído")) {
            throw new IllegalArgumentException("Não é possível aprovar: já existe um kit cadastrado com o nome '" + sol.getNome() + "'.");
        }

        // Converte a solicitação automaticamente em um Kit ativo
        Kit kit = new Kit(
                sol.getNome(),
                sol.getDescricao(),
                sol.getFinalidade(),
                sol.getQuantidadeDisponivel(),
                sol.getCategoria()
        );
        kit.setStatus("Disponível");

        for (SolicitacaoKitItem ski : sol.getItens()) {
            kit.getItens().add(new KitItem(kit, ski.getMaterial(), ski.getQuantidade()));
        }

        kit = kitRepository.save(kit);

        sol.setStatus("Aprovada");
        sol.setAprovador(admin);
        sol.setDataResposta(LocalDateTime.now());
        sol.setKitCriado(kit);

        return solicitacaoKitRepository.save(sol);
    }

    @Transactional
    public SolicitacaoKit recusar(Integer solicitacaoId, Integer adminId, String justificativa) {
        Usuario admin = validarAdministrador(adminId);

        SolicitacaoKit sol = solicitacaoKitRepository.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada."));

        if (!"Pendente".equalsIgnoreCase(sol.getStatus())) {
            throw new IllegalStateException("Esta solicitação já foi finalizada com status: " + sol.getStatus());
        }

        sol.setStatus("Recusada");
        sol.setAprovador(admin);
        sol.setDataResposta(LocalDateTime.now());
        if (justificativa != null && !justificativa.trim().isEmpty()) {
            sol.setJustificativa(justificativa.trim());
        }

        return solicitacaoKitRepository.save(sol);
    }
}
