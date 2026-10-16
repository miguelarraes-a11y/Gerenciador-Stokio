package com.senai.pi.stokio;

import com.senai.pi.stokio.models.*;
import com.senai.pi.stokio.repositories.*;
import com.senai.pi.stokio.services.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApplicationTests {

	@Autowired
	private MateriaisService materiaisService;

	@Autowired
	private MateriaisRepository materiaisRepository;

	@Autowired
	private KitService kitService;

	@Autowired
	private KitRepository kitRepository;

	@Autowired
	private SolicitacaoKitService solicitacaoKitService;

	@Autowired
	private SolicitacaoKitRepository solicitacaoKitRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@DisplayName("1. Estoque de Materiais: Baixa, Reposição e Impedimento de Retiradas Excessivas")
	void testGerenciamentoEstoqueMateriais() {
		Categoria cat = categoriaRepository.findAll().get(0);
		Usuario user = usuarioRepository.findAll().get(0);

		Materiais mat = new Materiais(null, "Cabo Jumper Especial", "Cabo Jumper Especial", 20, cat);
		Materiais matSalvo = materiaisService.salvar(mat);
		final Integer matId = matSalvo.getId();
		assertNotNull(matId);
		assertEquals(20, matSalvo.getQuantidadeDisponivel());

		// 1. Dar baixa com sucesso
		Materiais posBaixa = materiaisService.darBaixa(matId, 5, user.getId(), "Aula prática");
		assertEquals(15, posBaixa.getQuantidadeDisponivel());

		// 2. Impedir retirada superior à quantidade disponível
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			materiaisService.darBaixa(matId, 999, user.getId(), "Retirada excessiva inválida");
		});
		assertTrue(exception.getMessage().contains("não pode ser superior"));

		// 3. Repor quantidades
		Materiais posReposicao = materiaisService.repor(matId, 10, user.getId(), "Compra nova");
		assertEquals(25, posReposicao.getQuantidadeDisponivel());

		// 4. Exclusão de material
		materiaisService.deletar(matId);
		assertFalse(materiaisService.listar().stream().anyMatch(m -> m.getId().equals(matId)));
	}

	@Test
	@DisplayName("2. Kits: Exclusão Segura e Prevenção de Duplicidade")
	void testKitsExclusaoEDuplicidade() {
		Categoria cat = categoriaRepository.findAll().get(0);
		Materiais mat = materiaisRepository.findAll().get(0);

		Map<String, Object> payload = Map.of(
			"nome", "Kit Teste Robótica",
			"descricao", "Kit para testes de robótica",
			"quantidadeDisponivel", 3,
			"categoriaId", cat.getId(),
			"itens", List.of(Map.of("materialId", mat.getId(), "quantidade", 2))
		);

		Kit kit = kitService.salvarOuAtualizarKit(null, payload);
		final Integer kitId = kit.getId();
		assertNotNull(kitId);
		assertEquals("Kit Teste Robótica", kit.getNome());

		// Tentativa de duplicidade deve falhar
		assertThrows(IllegalArgumentException.class, () -> {
			kitService.salvarOuAtualizarKit(null, payload);
		});

		// Exclusão do kit
		kitService.deletar(kitId);
		assertFalse(kitService.listarTodos().stream().anyMatch(k -> k.getId().equals(kitId)));
	}

	@Test
	@DisplayName("3. Solicitação e Aprovação/Recusa de Novos Kits")
	void testSolicitacaoEAprovacaoDeKits() {
		Categoria cat = categoriaRepository.findAll().get(0);
		Materiais mat = materiaisRepository.findAll().get(0);
		Usuario admin = usuarioRepository.findAll().stream()
				.filter(u -> u.getTipoUsuario().toLowerCase().contains("admin"))
				.findFirst().orElseThrow();
		Usuario aluno = usuarioRepository.findAll().stream()
				.filter(u -> !u.getTipoUsuario().toLowerCase().contains("admin"))
				.findFirst().orElse(admin);

		// 1. Criar solicitação de kit
		Map<String, Object> payload = Map.of(
			"nome", "Kit Sensorica Avançada",
			"descricao", "Kit com sensores",
			"finalidade", "Projetos de IoT",
			"quantidadeDisponivel", 4,
			"categoriaId", cat.getId(),
			"solicitanteId", aluno.getId(),
			"itens", List.of(Map.of("materialId", mat.getId(), "quantidade", 3))
		);

		SolicitacaoKit solicitacao = solicitacaoKitService.criar(payload);
		assertNotNull(solicitacao.getId());
		assertEquals("Pendente", solicitacao.getStatus());
		assertNotNull(solicitacao.getDataSolicitacao());

		// 2. Não deve aparecer entre os kits disponíveis antes da aprovação
		assertFalse(kitService.listarTodos().stream().anyMatch(k -> k.getNome().equals("Kit Sensorica Avançada")));

		// 3. Prevenir solicitação duplicada
		assertThrows(IllegalArgumentException.class, () -> {
			solicitacaoKitService.criar(payload);
		});

		// 4. Usuário não-admin não pode aprovar
		if (!aluno.getId().equals(admin.getId())) {
			assertThrows(SecurityException.class, () -> {
				solicitacaoKitService.aprovar(solicitacao.getId(), aluno.getId());
			});
		}

		// 5. Administrador aprova solicitação
		SolicitacaoKit aprovada = solicitacaoKitService.aprovar(solicitacao.getId(), admin.getId());
		assertEquals("Aprovada", aprovada.getStatus());
		assertNotNull(aprovada.getAprovador());
		assertEquals(admin.getId(), aprovada.getAprovador().getId());
		assertNotNull(aprovada.getDataResposta());
		assertNotNull(aprovada.getKitCriado());

		// 6. Kit agora está disponível na listagem de kits
		assertTrue(kitService.listarTodos().stream().anyMatch(k -> k.getNome().equals("Kit Sensorica Avançada")));

		// 7. Testar recusa em uma nova solicitação
		Map<String, Object> payloadRecusa = Map.of(
			"nome", "Kit Para Recusar",
			"descricao", "Kit teste recusa",
			"categoriaId", cat.getId(),
			"solicitanteId", aluno.getId(),
			"itens", List.of(Map.of("materialId", mat.getId(), "quantidade", 1))
		);
		SolicitacaoKit solRecusa = solicitacaoKitService.criar(payloadRecusa);
		SolicitacaoKit recusada = solicitacaoKitService.recusar(solRecusa.getId(), admin.getId(), "Falta de componentes no momento");
		assertEquals("Recusada", recusada.getStatus());
		assertEquals("Falta de componentes no momento", recusada.getJustificativa());
		assertFalse(kitService.listarTodos().stream().anyMatch(k -> k.getNome().equals("Kit Para Recusar")));
	}
}
