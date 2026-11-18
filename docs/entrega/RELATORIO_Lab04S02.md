# Relatório Lab04S02 · Implantação (estado em 18/11/2026)

## Produção
| Recurso | Onde | Identificador |
|---|---|---|
| Front-end | https://moeda-estudantil-phi.vercel.app | Vercel, projeto `moeda-estudantil` (conta `gustavolimadias`) |
| API | https://moeda-estudantil-api-zj8w.onrender.com/api/health | Render, serviço `moeda-estudantil-api` (`srv-dbuv000n74is739h0kp0`) |
| Banco | Render PostgreSQL free 16, região oregon | `moeda-estudantil-db` (`dpg-dbuuviafngtc739u24m0-a`), **expira em 18/12/2026** |
| Código | https://github.com/GustavoLimaDias/Sistema-de-Moeda-Estudantil/tree/release-2 | branch `release-2` |

Entregáveis: `docs/entrega/Lab04S02_Moeda_Estudantil.pdf` (já postado), `docs/apresentacao/Lab04S02_Slides.pdf`,
`docs/uml/release2/implantacao.puml` e `.png`.

Os testes de produção de 18/11 passaram 33/33 (`docs/entrega/scripts/prod_test.py`). Eles alteraram saldos:
Carla Mendes 1000 → 800, Ana Souza 0 → 200 → 50 (resgatou o almoço, cupom `B56C67017AF8` conferido pela cantina).

## Como mudar algo
- **Front-end ou back-end:** basta `git push` em `release-2`. O Vercel publica em produção sozinho (branch de produção = `release-2`)
  e o Render faz novo build sozinho (auto-deploy ligado, cerca de 3 min). Todo push dispara o Render, mesmo que só mude docs.
- **Acompanhar deploys:** painel do Render → `moeda-estudantil-api` → *Events/Logs*; Vercel → projeto → *Deployments*.
- **Variáveis do back-end** (painel do Render → serviço → *Environment*): `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`
  (vindas do banco) e `CORS_ALLOWED_ORIGINS=https://moeda-estudantil-phi.vercel.app`. O serviço foi criado pela API do Render
  com os mesmos valores do `render.yaml`; o Blueprint não está sincronizado, então editar o `render.yaml` **não** altera o serviço.
- **Trocar a URL do Render** (se recriar o serviço): editar `destination` em `moeda-estudantil/src/main/resources/static/vercel.json`.
- **Zerar os dados de demonstração:** o `DataInitializer` só popula banco vazio. Apague e recrie o banco no Render
  (e atualize as 3 variáveis do banco no serviço) ou limpe as tabelas pelo `psql` liberando seu IP em *Networking*.
- **Regerar o PDF:** `python docs/entrega/scripts/gerar_pdf.py . https://moeda-estudantil-phi.vercel.app https://moeda-estudantil-api-zj8w.onrender.com`
- **Regerar os slides:** `python docs/entrega/scripts/gerar_slides.py .`
- **Regerar o diagrama:** `docker run --rm -v "%cd%/docs/uml/release2":/data -w /data plantuml/plantuml -tpng implantacao.puml`
- **Testar produção:** `python docs/entrega/scripts/prod_test.py https://moeda-estudantil-phi.vercel.app` (altera saldos).
- **Testes locais:** `cd moeda-estudantil && ./mvnw test` (ou via Docker: `mvn test` na imagem `maven:3.9-eclipse-temurin-17`).

## Pendências e ajustes opcionais
1. Revogar o token do GitHub (`ghp_…`, github.com/settings/tokens) e a API Key do Render usados no deploy.
2. Antes da apresentação: abrir o link ~2 min antes (cold start ~1 min). Opcional: UptimeRobot pingando `/api/health` a cada 10 min.
3. A nota dentro do diagrama ainda mostra `CORS_ALLOWED_ORIGINS = https://*.vercel.app`; o valor real é a origem exata do Vercel.
   Não alterei porque o PDF já foi postado e o diagrama precisa bater com ele.
4. No PDF, o parágrafo abaixo do diagrama transborda para uma 4ª página (cosmético).
5. O banco free expira em 18/12/2026: depois disso, criar outro e atualizar as variáveis do serviço.
6. Opcional: ativar 2FA na conta do Vercel.
