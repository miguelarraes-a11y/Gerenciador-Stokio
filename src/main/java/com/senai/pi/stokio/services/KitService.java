package com.senai.pi.stokio.services;

import com.senai.pi.stokio.models.Categoria;
import com.senai.pi.stokio.models.Kit;
import com.senai.pi.stokio.models.KitItem;
import com.senai.pi.stokio.models.Materiais;
import com.senai.pi.stokio.repositories.CategoriaRepository;
import com.senai.pi.stokio.repositories.KitRepository;
import com.senai.pi.stokio.repositories.MateriaisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class KitService {

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private MateriaisRepository materiaisRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Kit> listarTodos() {
        return kitRepository.findAll();
    }

    public Optional<Kit> buscarPorId(Integer id) {
        return kitRepository.findById(id);
    }

    public Kit salvarOuAtualizarKit(Integer id, Map<String, Object> payload) {
        Kit kit;
        if (id != null && kitRepository.existsById(id)) {
            kit = kitRepository.findById(id).get();
            kit.getItens().clear(); // Refresh items
        } else {
            kit = new Kit();
        }

        kit.setNome((String) payload.get("nome"));
        kit.setDescricao((String) payload.getOrDefault("descricao", ""));
        kit.setFinalidade((String) payload.getOrDefault("finalidade", ""));
        kit.setImagemUrl((String) payload.getOrDefault("imagemUrl", ""));

        if (payload.containsKey("quantidadeDisponivel")) {
            Object q = payload.get("quantidadeDisponivel");
            kit.setQuantidadeDisponivel(q instanceof Integer ? (Integer) q : Integer.parseInt(q.toString()));
        }

        if (payload.containsKey("status")) {
            kit.setStatus((String) payload.get("status"));
        }

        if (payload.containsKey("categoriaId") && payload.get("categoriaId") != null) {
            Integer catId = (Integer) payload.get("categoriaId");
            Optional<Categoria> c = categoriaRepository.findById(catId);
            c.ifPresent(kit::setCategoria);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itens = (List<Map<String, Object>>) payload.get("itens");

        if (itens != null) {
            for (Map<String, Object> itemData : itens) {
                Object matIdObj = itemData.get("materialId");
                int materialId = matIdObj instanceof Integer ? (Integer) matIdObj : Integer.parseInt(matIdObj.toString());
                
                Object qtdObj = itemData.getOrDefault("quantidade", 1);
                int qtd = qtdObj instanceof Integer ? (Integer) qtdObj : Integer.parseInt(qtdObj.toString());

                Optional<Materiais> mat = materiaisRepository.findById(materialId);
                mat.ifPresent(m -> {
                    KitItem ki = new KitItem(kit, m, qtd);
                    kit.getItens().add(ki);
                });
            }
        }

        return kitRepository.save(kit);
    }

    public void deletar(Integer id) {
        kitRepository.deleteById(id);
    }
}
