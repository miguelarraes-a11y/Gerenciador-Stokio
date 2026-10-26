
# Projeto: Aplicativo de Controle de Kits e Objetos do Fablab (SENAI)

## Integrantes
- Benício Guimarães
- Cauã França
- Gustavo Justen
- João Bernardo
- Miguel Campião

## Descrição
O projeto consiste no desenvolvimento de um **aplicativo para controle de kits e objetos do Fablab (SENAI)**. A proposta é criar uma **interface interativa e prática**, facilitando a organização, o acompanhamento e o gerenciamento dos equipamentos disponíveis no laboratório.

## Objetivo
O objetivo do aplicativo é **melhorar o controle de empréstimos, localização e manutenção de kits e itens**, tornando o processo mais organizado e eficiente para os usuários e responsáveis pelo Fablab.

## Funcionalidades

### 1. Lista de Todos os Kits
- Exibe todos os kits disponíveis no Fablab.
- Permite visualizar rapidamente quais itens existem no laboratório.

### 2. Lista de Empréstimos
- Mostra **quem pegou determinado kit ou item**.
- Ajuda no controle de uso e devolução dos equipamentos.

### 3. Lista de Manutenção
- Exibe **kits e itens que estão em manutenção**.
- Facilita a identificação de equipamentos temporariamente indisponíveis.

## Gerenciamento das Listas
O usuário do aplicativo poderá **editar todas as listas**, permitindo:

- Adicionar novos kits ou itens
- Remover kits ou itens
- Colocar kits ou itens em manutenção
- Retirar kits ou itens da manutenção
- Atualizar informações de empréstimo

## Conclusão
Com esse sistema, o Fablab poderá ter **maior organização e controle sobre seus equipamentos**, tornando o processo de uso mais eficiente, transparente e fácil de gerenciar.

---

## Como Executar o Projeto (Compatibilidade Windows e Linux)

O sistema **STOKIO** é 100% compatível tanto com **Windows** quanto com **Linux** (e macOS), utilizando o Maven Wrapper incluído no repositório.

### Pré-requisitos
- **Java JDK 17** ou superior instalado e configurado nas variáveis de ambiente (`JAVA_HOME` / `PATH`).
- Navegador Web moderno (Google Chrome, Firefox, Edge, etc.).

---

### Execução no Windows

1. Abra o terminal (PowerShell ou Prompt de Comando) na pasta raiz do projeto:
   ```cmd
   cd C:\caminho\para\projeto-integrador-main
   ```
2. Inicie a aplicação com o Maven Wrapper:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
3. Abra no navegador:
   ```text
   http://localhost:8080
   ```

Para rodar os testes automatizados no Windows:
```powershell
.\mvnw.cmd test
```

---

### Execução no Linux / macOS

1. Abra o terminal no diretório do projeto:
   ```bash
   cd /caminho/para/projeto-integrador-main
   ```
2. Garanta permissão de execução ao script wrapper (caso clonado sem a flag de execução):
   ```bash
   chmod +x ./mvnw
   ```
3. Inicie a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Abra no navegador:
   ```text
   http://localhost:8080
   ```

Para rodar os testes automatizados no Linux:
```bash
./mvnw test
```

---

## Perfis de Usuário e Credenciais Padrão (Ambiente de Testes)

O banco de dados H2 em memória já inicia com usuários pré-configurados para validação de cada perfil:

| Perfil | Nome | E-mail | Senha | Permissões Principais |
| :--- | :--- | :--- | :--- | :--- |
| **Administrador** | Administrador SENAI | `admin@stokio.senai.br` | `123456` | Acesso total: gestão de estoque, reposição, baixa, exclusão, criação/exclusão de kits, aprovação de solicitações de professores e alunos, gestão de usuários e permissões. |
| **Professor** | Prof. Carlos Eduardo | `carlos.eduardo@senai.br` | `123456` | Visualiza estoque e kits; solicita empréstimo de materiais e kits; cria solicitações de kits pedagógicos (para aprovação do admin); realiza agendamentos; aprova/recusa solicitações de alunos sob sua responsabilidade. Não pode alterar estoque nem editar kits existentes. |
| **Aluno** | Lucas Silva | `lucas.silva@aluno.senai.br` | `123456` | Visualiza kits disponíveis para empréstimo e horários de laboratório; solicita empréstimo de kit ou agendamento de espaço selecionando obrigatoriamente um professor responsável; aguarda aprovação. Não acessa estoque de materiais brutos nem funções administrativas. |

