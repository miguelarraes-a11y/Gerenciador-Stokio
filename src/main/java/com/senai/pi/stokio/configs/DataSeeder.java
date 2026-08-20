package com.senai.pi.stokio.configs;

import com.senai.pi.stokio.models.*;
import com.senai.pi.stokio.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MateriaisRepository materiaisRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AgendamentoLaboratorioRepository labRepo;

    @Autowired
    private AgendamentoMaterialRepository matRepo;

    @Autowired
    private EmprestimoRepository emprestimoRepo;

    @Autowired
    private KitRepository kitRepo;

    @Override
    public void run(String... args) throws Exception {
        Usuario admin = null;
        Usuario professor = null;
        Usuario aluno = null;

        if (usuarioRepository.count() == 0) {
            admin = new Usuario(null, "Administrador SENAI", "admin@stokio.senai.br", "123456", "admin", "SP");
            admin.setTelefone("(11) 98765-4321");
            admin = usuarioRepository.save(admin);

            professor = new Usuario(null, "Prof. Carlos Eduardo", "carlos.eduardo@senai.br", "123456", "professor", "SP");
            professor.setTelefone("(11) 91234-5678");
            professor = usuarioRepository.save(professor);

            aluno = new Usuario(null, "Lucas Silva", "lucas.silva@aluno.senai.br", "123456", "aluno", "SP");
            aluno.setTelefone("(11) 95555-4444");
            aluno = usuarioRepository.save(aluno);
        } else {
            List<Usuario> list = usuarioRepository.findAll();
            admin = list.get(0);
            if (list.size() > 1) professor = list.get(1);
            if (list.size() > 2) aluno = list.get(2);
        }

        Map<String, List<String>> officialCategoriesWithItems = new LinkedHashMap<>();
        officialCategoriesWithItems.put("Máquinas e Equipamentos Principais", Arrays.asList(
            "Impressoras 3D", "Máquinas de corte a laser", "Máquinas de gravação a laser", 
            "Fresadoras CNC", "Fresadoras de precisão", "Plotters de recorte", 
            "Computadores CAD/CAM", "Equipamentos de modelagem digital", 
            "Equipamentos de digitalização / escaneamento 3D", "Osciloscópio", "Sugador de solda", "Fonte de bancada"
        ));
        officialCategoriesWithItems.put("Placas e Microcontroladores", Arrays.asList(
            "Arduino Uno", "Arduino Mega", "Arduino Nano", "ESP32", "ESP8266"
        ));
        officialCategoriesWithItems.put("Protótipos e Conexões", Arrays.asList(
            "Protoboards", "Jumpers macho-macho", "Jumpers macho-fêmea", "Cabos USB", "Conectores"
        ));
        officialCategoriesWithItems.put("Sensores", Arrays.asList(
            "Sensores de temperatura", "Sensores ultrassônicos", "Sensor ultrassônico HC-SR04", "Sensor PIR"
        ));
        officialCategoriesWithItems.put("Atuadores e Automação", Arrays.asList(
            "Servomotores", "Motores DC", "Relés", "Solenoides"
        ));
        officialCategoriesWithItems.put("Componentes Eletrônicos", Arrays.asList(
            "LEDs", "Botões", "Potenciômetros"
        ));
        officialCategoriesWithItems.put("Ferramentas Eletrônicas", Arrays.asList(
            "Ferro de solda", "Multímetro"
        ));
        officialCategoriesWithItems.put("Materiais de Fabricação", Arrays.asList(
            "Filamento PLA", "Filamento ABS", "Filamento PETG", "PLA", "ABS", "PETG", "MDF", "Acrílico", "Compensado", "Alumínio leve"
        ));
        officialCategoriesWithItems.put("Ferramentas Maker e Oficina", Arrays.asList(
            "Furadeira", "Mini retífica", "Lixas", "Alicates", "Parafusos", "Porcas", "Fitas de fixação"
        ));
        officialCategoriesWithItems.put("Energia", Arrays.asList(
            "Baterias"
        ));

        List<Materiais> savedMaterials = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : officialCategoriesWithItems.entrySet()) {
            String catName = entry.getKey();
            Optional<Categoria> existingCat = categoriaRepository.findAll().stream()
                    .filter(c -> c.getNome().equalsIgnoreCase(catName))
                    .findFirst();
            
            Categoria cat;
            if (existingCat.isPresent()) {
                cat = existingCat.get();
            } else {
                cat = categoriaRepository.save(new Categoria(null, catName));
            }

            List<String> items = entry.getValue();
            int defaultQty = 12;
            for (String itemName : items) {
                boolean itemExists = materiaisRepository.findAll().stream()
                        .anyMatch(m -> m.getNome() != null && m.getNome().equalsIgnoreCase(itemName));
                if (!itemExists) {
                    Materiais mat = new Materiais(null, itemName, itemName, defaultQty, cat);
                    mat.setStatus("Disponível");
                    mat.setEstadoConservacao("Excelente");
                    mat.setEstoqueMinimo(5);
                    mat.setObservacoes("Item cadastrado automaticamente no banco de dados STOKIO.");
                    savedMaterials.add(materiaisRepository.save(mat));
                    defaultQty = (defaultQty % 20) + 5;
                } else {
                    savedMaterials.add(materiaisRepository.findAll().stream().filter(m -> m.getNome().equalsIgnoreCase(itemName)).findFirst().get());
                }
            }
        }

        // Seed Sample Kit: "Kit Arduino Básico" if empty
        if (kitRepo.count() == 0) {
            Categoria catPlacas = categoriaRepository.findAll().stream().filter(c -> c.getNome().contains("Placas")).findFirst().orElse(null);
            
            Kit kit = new Kit(
                "Kit Arduino Básico",
                "Kit destinado ao aprendizado de eletrônica básica, programação embarcada e prototipagem de projetos.",
                "Utilizado em oficinas, aulas práticas e projetos maker.",
                5,
                catPlacas
            );
            kit.setStatus("Disponível");

            // Add component items to Kit
            Materiais arduino = savedMaterials.stream().filter(m -> m.getNome().equalsIgnoreCase("Arduino Uno")).findFirst().orElse(null);
            Materiais protoboard = savedMaterials.stream().filter(m -> m.getNome().equalsIgnoreCase("Protoboards")).findFirst().orElse(null);
            Materiais leds = savedMaterials.stream().filter(m -> m.getNome().equalsIgnoreCase("LEDs")).findFirst().orElse(null);
            Materiais jumpers = savedMaterials.stream().filter(m -> m.getNome().toLowerCase().contains("jumpers")).findFirst().orElse(null);
            Materiais caboUsb = savedMaterials.stream().filter(m -> m.getNome().equalsIgnoreCase("Cabos USB")).findFirst().orElse(null);

            if (arduino != null) kit.getItens().add(new KitItem(kit, arduino, 1));
            if (protoboard != null) kit.getItens().add(new KitItem(kit, protoboard, 1));
            if (leds != null) kit.getItens().add(new KitItem(kit, leds, 5));
            if (jumpers != null) kit.getItens().add(new KitItem(kit, jumpers, 20));
            if (caboUsb != null) kit.getItens().add(new KitItem(kit, caboUsb, 1));

            kitRepo.save(kit);
        }

        if (labRepo.count() == 0 && professor != null) {
            AgendamentoLaboratorio lab1 = new AgendamentoLaboratorio("Fab Lab Main", professor, LocalDate.now().plusDays(1), LocalTime.of(14, 0), LocalTime.of(16, 0), "Aula de Prototipagem");
            lab1.setStatus("APROVADO");
            labRepo.save(lab1);
        }

        if (emprestimoRepo.count() == 0 && !savedMaterials.isEmpty() && aluno != null) {
            Materiais mat = savedMaterials.get(0);
            Emprestimo emp1 = new Emprestimo(mat, mat.getCategorias(), 2, aluno, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2), "Projeto Maker");
            emp1.setStatus("Retirado");
            emprestimoRepo.save(emp1);
        }
    }
}
