# Sistema de Moeda Estudantil · Lab03S02

Laboratório 3 de Projeto de Software (PUC Minas, Engenharia de Software, 2º/2026, Profa. Milena Menezes Adão).
Integrante: **Gustavo Lima Dias**

Entregas da **Sprint 2**:

- Modelo ER com o modelo físico (`docs/modelo_er.png`, `docs/schema.sql`)
- Estratégia de acesso a dados: ORM com JPA/Hibernate e Repository/DAO com Spring Data JPA
- CRUDs de **aluno** e **empresa parceira** (com as vantagens de cada empresa): front-end mais back-end em arquitetura MVC
- Modelos UML atualizados (v2), com a versão da Sprint 1 preservada

## Estrutura

```
moeda-estudantil/                      # aplicação Spring Boot
  src/main/java/br/pucminas/moedaestudantil/
    model/        # entidades JPA (Usuario, Aluno, Professor, EmpresaParceira, Vantagem, Transacao...)
    repository/   # DAO: interfaces Spring Data JPA
    service/      # regras de negócio (@Transactional)
    controller/   # API REST (/api/alunos, /api/empresas, /api/instituicoes)
    dto/          # contratos de entrada/saída da API
    exception/    # tratamento padronizado de erros
    config/       # BCrypt, CORS e carga inicial de dados
  src/main/resources/static/           # front-end (HTML/CSS/JS)
  src/test/                            # testes (JUnit + MockMvc)
docs/
  Lab03S02_Moeda_Estudantil.pdf        # documento da sprint (ER, persistência, API, telas)
  modelo_er.puml / modelo_er.png
  schema.sql                           # DDL PostgreSQL de referência
  uml/sprint1/                         # modelos da Sprint 1
  uml/sprint2/                         # diagramas de classes e componentes v2
```

## Como executar

Requisito: **Java 17+**. O Maven é baixado pelo wrapper na primeira execução.

```bash
cd moeda-estudantil
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Abra **http://localhost:8080**.

| Recurso | Endereço |
|---|---|
| Front-end | http://localhost:8080 |
| API | http://localhost:8080/api/alunos, /api/empresas, /api/instituicoes |
| Console H2 | http://localhost:8080/h2-console (JDBC `jdbc:h2:file:./data/moedaestudantil`, usuário `sa`, senha vazia) |

Testes: `./mvnw test`.
PostgreSQL (opcional): crie o banco `moeda_estudantil` e rode `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres`.

No primeiro start o sistema carrega 3 instituições, 2 professores (1.000 moedas em 2026/2), 1 administrador,
1 aluno e 2 empresas com vantagens. A senha de todos é `123456`. Para recomeçar do zero, apague a pasta `data/`.

## Próxima sprint (Lab03S03)

CRUDs finais com login, envio de moedas com email, resgate de vantagem com cupom, extrato e slides da arquitetura e da persistência.
