# Sistema de Moeda Estudantil · versão final (Lab03S03) - Lab04-release 1

Laboratório 4 de Projeto de Software (PUC Minas, Engenharia de Software, 2º/2026, Profa. Milena Menezes Adão).
Integrante: **Gustavo Lima Dias**

Professores reconhecem o mérito dos alunos com moedas virtuais. Os alunos trocam essas moedas por vantagens
oferecidas por empresas parceiras e recebem um cupom com código para a troca presencial.

## Funcionalidades (todos os requisitos do enunciado)

| Requisito | Onde |
|---|---|
| Login e senha para alunos, professores e empresas; autenticação em todas as operações | `AuthController`, `AutenticacaoInterceptor`, `@RequerPerfil` |
| Cadastro de aluno (nome, email, CPF, RG, endereço, instituição pré-cadastrada, curso), CRUD final | `cadastro.html`, `aluno.html` (perfil), `admin-alunos.html` |
| Professores pré-cadastrados (nome, CPF, departamento, vínculo com a instituição), importação da lista | `admin.html` → `/api/professores/importacao` |
| 1.000 moedas por semestre, acumulativo | `CreditoSemestralService` (`@Scheduled` em 1º/jan e 1º/jul, mais a inicialização) |
| Envio de moedas com saldo suficiente e motivo obrigatório | `professor.html` → `TransacaoService.enviarMoedas` |
| Email ao aluno quando recebe moedas | `NotificacaoService` (evento após o commit) |
| Extrato de professor (envios) e aluno (recebimentos e trocas) | `/api/professores/me/extrato`, `/api/alunos/me/extrato` |
| Cadastro de empresa e de vantagens (descrição, foto, custo em moedas), CRUD final | `cadastro.html`, `empresa.html`, `admin-empresas.html` |
| Resgate debitando o saldo, com código gerado e emails de cupom (aluno) e de conferência (empresa) | `aluno.html` → `TransacaoService.resgatarVantagem` |
| Conferência do cupom pela empresa | `empresa.html` → `/api/empresas/me/resgates/{codigo}/conferir` |

## Arquitetura (MVC)

```
View        src/main/resources/static/   HTML + CSS + JS (fetch na API REST, token Bearer)
Segurança   security/                    AutenticacaoInterceptor, TokenService, @Publico, @RequerPerfil
Controller  controller/                  Auth, Aluno, Professor, EmpresaParceira, Vantagem, Instituicao, Admin
Model       service/                     regras de negócio (@Transactional), eventos e emails
            model/                       entidades JPA (herança JOINED: Usuario e Transacao)
            repository/                  Spring Data JPA (padrão Repository/DAO)
Banco       H2 em arquivo (padrão) ou PostgreSQL (perfil "postgres")
```

Os diagramas estão em `docs/uml/sprint3`: componentes, classes e as sequências do envio e do resgate.
Em `docs/` ficam o modelo ER (`modelo_er.png`), o DDL (`schema.sql`) e as telas.
As versões das sprints 1 e 2 continuam em `docs/uml/sprint1` e `docs/uml/sprint2`.

## Como executar

Requisito: **Java 17+**. O Maven é baixado pelo wrapper na primeira execução.

```bash
cd moeda-estudantil
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Abra **http://localhost:8080** e entre com uma das contas de demonstração (senha `123456`):

| Perfil | Login |
|---|---|
| Aluno | `ana.souza`, `lucas.pereira` (PUC Minas), `marina.costa` (UFMG) |
| Professor | `carla.mendes`, `ricardo.alves` (PUC Minas) |
| Empresa parceira | `cantina`, `papelaria` |
| Administrador | `admin` |

**Roteiro de demonstração:**
1. Entre como `carla.mendes` e envie moedas para Ana Souza.
2. Entre como `ana.souza`: veja o saldo, o extrato e o email recebido, depois resgate uma vantagem e anote o cupom.
3. Entre como `cantina`, abra *Resgates e cupons* e confira o código.

Outros comandos:
- Testes automatizados: `./mvnw test` (H2 em memória; cobrem CRUDs, perfis de acesso, envio, resgate, cupom, emails, crédito semestral e importação).
- Console do banco: http://localhost:8080/h2-console (JDBC `jdbc:h2:file:./data/moedaestudantil`, usuário `sa`, senha vazia).
- Recomeçar do zero: apague a pasta `moeda-estudantil/data/`. Faça isso também se você rodou a versão da Sprint 2, porque o banco ganhou tabelas novas.

### Emails

Sem SMTP configurado, os emails ficam em **modo simulado**. Eles são gravados na tabela `notificacao_email`
e aparecem na aba *Emails* de cada usuário e na administração. Para enviar de verdade, configure
`spring.mail.*` em `application.properties` (o arquivo já traz o exemplo do Gmail com senha de app).

### PostgreSQL (opcional)

Crie o banco `moeda_estudantil` e rode `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres`.
