# Sistema de Moeda Estudantil · Release 2 (Lab04S01)

Laboratório 3 de Projeto de Software (PUC Minas, Engenharia de Software, 2º/2026, Profa. Milena Menezes Adão).
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

## Release 2 (Lab04S01) · branch `release-2`

Base: tag `release-1` (fim do Lab 3). Novos casos de uso e endpoints:

| UC | Caso de uso | Endpoint | Perfil |
|---|---|---|---|
| UC13 | Excluir instituição (sem vínculos) | `DELETE /api/instituicoes/{id}` | ADMIN |
| UC14 | Editar / excluir professor (sem envios) | `PUT` e `DELETE /api/professores/{id}` | ADMIN |
| UC15 | Alterar senha (botão **Senha** no topo) | `PUT /api/auth/senha` | qualquer |
| UC17 | Cancelar cupom pendente, com estorno | `POST /api/alunos/me/resgates/{codigo}/cancelar` | ALUNO |
| UC17 | Cancelar cupom pendente, com estorno | `POST /api/empresas/me/resgates/{codigo}/cancelar` | EMPRESA |
| UC18 | Email de cancelamento para aluno e empresa | (automático, após o commit) | — |

- O cancelamento devolve as moedas e aparece como **ESTORNO** no extrato; cupom já conferido ou cancelado → 422.
- Nova coluna `resgate_vantagem.data_cancelamento`: apague a pasta `data/` antes de rodar se você já tinha o banco H2 da Release 1.
- Testes novos em `Release2IntegrationTest`.

Modelagem em `docs/uml/release2`: casos de uso v2, 14 diagramas de sequência (`sequencia/`), diagrama de visão geral de interação, classes v4, componentes v4, ER v3 e o PDF da entrega (`Lab04S01_Moeda_Estudantil.pdf`).

## Release 2 – Lab04S02 (Implantação)

| | URL |
|---|---|
| Sistema (front-end, Vercel) | https://moeda-estudantil-phi.vercel.app |
| API (back-end, Render) | https://moeda-estudantil-api-zj8w.onrender.com/api/health → `{"status":"UP"}` |

O front-end é o mesmo de `src/main/resources/static`: o Vercel publica essa pasta e o `vercel.json` repassa
`/api/*` para o Render. Assim o navegador fala só com o domínio do Vercel e o `api.js` continua com URLs relativas.
Localmente o Spring continua servindo o mesmo front em http://localhost:8080.

**Deploy**
1. Render: *New → Blueprint* → este repositório, branch `release-2` → *Apply*. O `render.yaml` da raiz cria o
   PostgreSQL `moeda-estudantil-db` e o Web Service Docker `moeda-estudantil-api` (plano free, health check `/api/health`).
2. Vercel: *Add New → Project* → este repositório → Root Directory `moeda-estudantil/src/main/resources/static`,
   Framework Preset *Other*, sem build command → *Deploy*.
3. O `static/vercel.json` aponta `/api/*` para o serviço do Render (troque a URL se recriar o serviço).

**Variáveis de ambiente (perfil `prod`)**

| Variável | Origem no Render | Uso |
|---|---|---|
| `DATABASE_URL` | `fromDatabase.connectionString` | convertida para `DB_URL` JDBC no `ENTRYPOINT` do Dockerfile |
| `DB_URL` | (opcional) | URL JDBC direta, ex.: `jdbc:postgresql://host:5432/moeda_estudantil` |
| `DB_USERNAME` / `DB_PASSWORD` | `fromDatabase.user` / `password` | credenciais do banco |
| `CORS_ALLOWED_ORIGINS` | `https://moeda-estudantil-phi.vercel.app` | origens extras de CORS, separadas por vírgula (localhost sempre liberado) |
| `PORT` | definida pelo Render | porta HTTP |

Os emails continuam em modo simulado em produção (o plano free do Render bloqueia SMTP).
O serviço free "dorme" após 15 minutos sem uso (o primeiro acesso leva cerca de 1 minuto) e o PostgreSQL free expira em 30 dias.

**Rodar o perfil `prod` localmente (Docker)**

```bash
docker network create moeda-net
docker run -d --name moeda-pg --network moeda-net -e POSTGRES_DB=moeda_estudantil \
  -e POSTGRES_USER=moeda -e POSTGRES_PASSWORD=troque-me postgres:16
docker build -t moeda-api moeda-estudantil
docker run --rm --network moeda-net -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://moeda-pg:5432/moeda_estudantil \
  -e DB_USERNAME=moeda -e DB_PASSWORD=troque-me moeda-api
```

Diagrama de implantação: [`docs/uml/release2/implantacao.png`](docs/uml/release2/implantacao.png) (fonte `implantacao.puml`).
PDF da entrega (link do sistema e acessos): [`docs/entrega/Lab04S02_Moeda_Estudantil.pdf`](docs/entrega/Lab04S02_Moeda_Estudantil.pdf).

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
