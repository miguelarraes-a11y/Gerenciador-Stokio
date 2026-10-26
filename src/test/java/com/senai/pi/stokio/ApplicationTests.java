package com.senai.pi.stokio;

import com.senai.pi.stokio.controllers.*;
import com.senai.pi.stokio.models.*;
import com.senai.pi.stokio.repositories.*;
import com.senai.pi.stokio.services.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

	@Autowired
	private EmprestimoKitController emprestimoKitController;

	@Autowired
	private AgendamentoLaboratorioController agendamentoLaboratorioController;

	@Autowired
	private EmprestimoController emprestimoController;

	@Autowired
	private KitController kitController;

	@Autowired
	private MateriaisController materiaisController;

	@Autowired
	private UsuarioController usuarioController;

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

	@Test
	@DisplayName("4. Empréstimo de Kits: Exigência de Professor Responsável e Fluxo de Aprovação")
	void testEmprestimoKitAlunoComProfessorResponsavel() {
		Kit kit = kitRepository.findAll().stream().filter(k -> k.getQuantidadeDisponivel() > 0).findFirst().orElseThrow();
		int qtdInicial = kit.getQuantidadeDisponivel();
		Usuario aluno = usuarioRepository.findAll().stream().filter(Usuario::isAluno).findFirst().orElseThrow();
		Usuario professor = usuarioRepository.findAll().stream().filter(Usuario::isProfessor).findFirst().orElseThrow();
		Usuario outroProfessor = new Usuario(null, "Prof. Secundário", "secundario@senai.br", "123456", "professor", "SP");
		outroProfessor = usuarioRepository.save(outroProfessor);

		// 1. Aluno sem professor responsável deve receber Bad Request (400)
		EmprestimoKit empSemProf = new EmprestimoKit();
		empSemProf.setKit(kit);
		empSemProf.setSolicitante(aluno);
		empSemProf.setQuantidade(1);
		ResponseEntity<?> respSemProf = emprestimoKitController.solicitar(empSemProf);
		assertEquals(HttpStatus.BAD_REQUEST, respSemProf.getStatusCode());

		// 2. Aluno com professor responsável deve ser criado com status Pendente
		EmprestimoKit empComProf = new EmprestimoKit();
		empComProf.setKit(kit);
		empComProf.setSolicitante(aluno);
		empComProf.setProfessorResponsavel(professor);
		empComProf.setQuantidade(1);
		empComProf.setDataDevolucaoPrevista(LocalDateTime.now().plusDays(2));
		ResponseEntity<?> respComProf = emprestimoKitController.solicitar(empComProf);
		assertEquals(HttpStatus.OK, respComProf.getStatusCode());
		EmprestimoKit salvo = (EmprestimoKit) respComProf.getBody();
		assertNotNull(salvo);
		assertEquals("Pendente", salvo.getStatus());
		assertEquals(professor.getId(), salvo.getProfessorResponsavel().getId());

		// 3. Aluno não pode aprovar seu próprio empréstimo (403)
		ResponseEntity<?> respAutoAprov = emprestimoKitController.aprovar(salvo.getId(), aluno.getId());
		assertEquals(HttpStatus.FORBIDDEN, respAutoAprov.getStatusCode());

		// 4. Outro professor não designado não pode aprovar (403)
		ResponseEntity<?> respOutroProf = emprestimoKitController.aprovar(salvo.getId(), outroProfessor.getId());
		assertEquals(HttpStatus.FORBIDDEN, respOutroProf.getStatusCode());

		// 5. Professor responsável designado aprova com sucesso (200)
		ResponseEntity<?> respAprovado = emprestimoKitController.aprovar(salvo.getId(), professor.getId());
		assertEquals(HttpStatus.OK, respAprovado.getStatusCode());
		EmprestimoKit aprovado = (EmprestimoKit) respAprovado.getBody();
		assertEquals("Aprovado", aprovado.getStatus());
		assertNotNull(aprovado.getAprovadoPor());
		assertEquals(professor.getId(), aprovado.getAprovadoPor().getId());

		// 6. Retirada reduz a quantidade disponível do kit
		ResponseEntity<?> respRetirada = emprestimoKitController.retirar(salvo.getId(), professor.getId());
		assertEquals(HttpStatus.OK, respRetirada.getStatusCode());
		Kit kitAposRetirada = kitRepository.findById(kit.getId()).orElseThrow();
		assertEquals(qtdInicial - 1, kitAposRetirada.getQuantidadeDisponivel());

		// 7. Devolução restaura a quantidade disponível do kit
		ResponseEntity<?> respDevolucao = emprestimoKitController.devolver(salvo.getId(), professor.getId());
		assertEquals(HttpStatus.OK, respDevolucao.getStatusCode());
		Kit kitAposDevolucao = kitRepository.findById(kit.getId()).orElseThrow();
		assertEquals(qtdInicial, kitAposDevolucao.getQuantidadeDisponivel());
	}

	@Test
	@DisplayName("5. Agendamento de Espaços: Exigência de Professor Responsável e Aprovação")
	void testAgendamentoLaboratorioAlunoComProfessorResponsavel() {
		Usuario aluno = usuarioRepository.findAll().stream().filter(Usuario::isAluno).findFirst().orElseThrow();
		Usuario professor = usuarioRepository.findAll().stream().filter(Usuario::isProfessor).findFirst().orElseThrow();

		// 1. Agendamento por aluno sem professor responsável é rejeitado (400)
		AgendamentoLaboratorio agSemProf = new AgendamentoLaboratorio("Fab Lab Impressão 3D", aluno, LocalDate.now().plusDays(10), LocalTime.of(9, 0), LocalTime.of(11, 0), "Projeto Final");
		ResponseEntity<?> respSemProf = agendamentoLaboratorioController.criar(agSemProf);
		assertEquals(HttpStatus.BAD_REQUEST, respSemProf.getStatusCode());

		// 2. Agendamento por aluno com professor responsável fica Pendente (200)
		AgendamentoLaboratorio agComProf = new AgendamentoLaboratorio("Fab Lab Impressão 3D", aluno, LocalDate.now().plusDays(10), LocalTime.of(9, 0), LocalTime.of(11, 0), "Projeto Final");
		agComProf.setProfessorResponsavel(professor);
		ResponseEntity<?> respComProf = agendamentoLaboratorioController.criar(agComProf);
		assertEquals(HttpStatus.OK, respComProf.getStatusCode());
		AgendamentoLaboratorio salvo = (AgendamentoLaboratorio) respComProf.getBody();
		assertNotNull(salvo);
		assertEquals("PENDENTE", salvo.getStatus());
		assertEquals(professor.getId(), salvo.getProfessorResponsavel().getId());

		// 3. Professor responsável aprova
		ResponseEntity<?> respAprov = agendamentoLaboratorioController.aprovar(salvo.getId(), professor.getId());
		assertEquals(HttpStatus.OK, respAprov.getStatusCode());
		AgendamentoLaboratorio aprovado = (AgendamentoLaboratorio) respAprov.getBody();
		assertEquals("APROVADO", aprovado.getStatus());
		assertEquals(professor.getId(), aprovado.getAprovadoPor().getId());
	}

	@Test
	@DisplayName("6. Empréstimo de Materiais e Restrição de Acesso por Tipo de Usuário")
	void testEmprestimoMateriaisRestrito() {
		Materiais mat = materiaisRepository.findAll().stream().filter(m -> m.getQuantidadeDisponivel() > 0).findFirst().orElseThrow();
		Usuario aluno = usuarioRepository.findAll().stream().filter(Usuario::isAluno).findFirst().orElseThrow();
		Usuario professor = usuarioRepository.findAll().stream().filter(Usuario::isProfessor).findFirst().orElseThrow();
		Usuario admin = usuarioRepository.findAll().stream().filter(Usuario::isAdmin).findFirst().orElseThrow();

		// 1. Aluno não pode solicitar empréstimo de materiais brutos diretamente (403)
		Emprestimo empAluno = new Emprestimo(mat, mat.getCategorias(), 1, aluno, LocalDateTime.now(), LocalDateTime.now().plusDays(1), "Teste");
		ResponseEntity<?> respAluno = emprestimoController.solicitar(empAluno);
		assertEquals(HttpStatus.FORBIDDEN, respAluno.getStatusCode());

		// 2. Professor pode solicitar empréstimo de materiais (status Pendente para admin aprovar)
		Emprestimo empProf = new Emprestimo(mat, mat.getCategorias(), 1, professor, LocalDateTime.now(), LocalDateTime.now().plusDays(2), "Aula prática");
		ResponseEntity<?> respProf = emprestimoController.solicitar(empProf);
		assertEquals(HttpStatus.OK, respProf.getStatusCode());
		Emprestimo salvo = (Emprestimo) respProf.getBody();
		assertNotNull(salvo);
		assertEquals("Pendente", salvo.getStatus());

		// 3. Admin aprova o empréstimo de material
		ResponseEntity<?> respAprov = emprestimoController.aprovar(salvo.getId(), admin.getId());
		assertEquals(HttpStatus.OK, respAprov.getStatusCode());
		Emprestimo aprovado = (Emprestimo) respAprov.getBody();
		assertEquals("Aprovado", aprovado.getStatus());
	}

	@Test
	@DisplayName("7. Permissões Administrativas e Bloqueio de Operações Não Autorizadas")
	void testPermissoesAcessoAdmin() {
		Usuario admin = usuarioRepository.findAll().stream().filter(Usuario::isAdmin).findFirst().orElseThrow();
		Usuario professor = usuarioRepository.findAll().stream().filter(Usuario::isProfessor).findFirst().orElseThrow();
		Usuario aluno = usuarioRepository.findAll().stream().filter(Usuario::isAluno).findFirst().orElseThrow();
		Materiais mat = materiaisRepository.findAll().get(0);
		Kit kit = kitRepository.findAll().get(0);

		// 1. Professor não pode alterar tipo de usuário (403)
		ResponseEntity<?> respTrocaProf = usuarioController.atualizarTipo(aluno.getId(), professor.getId(), Map.of("tipoUsuario", "admin"));
		assertEquals(403, respTrocaProf.getStatusCode().value());

		// 2. Admin pode alterar permissão de usuário (200)
		ResponseEntity<?> respTrocaAdmin = usuarioController.atualizarTipo(aluno.getId(), admin.getId(), Map.of("tipoUsuario", "aluno"));
		assertEquals(HttpStatus.OK, respTrocaAdmin.getStatusCode());

		// 3. Professor não pode excluir kits diretamente (403)
		ResponseEntity<?> respDelKit = kitController.deletar(kit.getId(), professor.getId());
		assertEquals(HttpStatus.FORBIDDEN, respDelKit.getStatusCode());

		// 4. Professor não pode dar baixa direta no estoque de materiais (403)
		ResponseEntity<?> respBaixaProf = materiaisController.darBaixa(mat.getId(), professor.getId(), Map.of("quantidade", 1, "motivo", "Tentativa direta"));
		assertEquals(HttpStatus.FORBIDDEN, respBaixaProf.getStatusCode());

		// 5. Professor não pode repor estoque diretamente (403)
		ResponseEntity<?> respReporProf = materiaisController.repor(mat.getId(), professor.getId(), Map.of("quantidade", 5, "motivo", "Tentativa direta"));
		assertEquals(HttpStatus.FORBIDDEN, respReporProf.getStatusCode());
	}
}
