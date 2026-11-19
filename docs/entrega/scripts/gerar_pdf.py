"""Builds docs/entrega/Lab04S02_Moeda_Estudantil.pdf (HTML -> PDF via headless Chrome).
Usage: python docs/entrega/scripts/gerar_pdf.py . https://moeda-estudantil-phi.vercel.app https://moeda-estudantil-api-zj8w.onrender.com
"""
import base64
import html
import pathlib
import subprocess
import sys
import tempfile
from datetime import date

root = pathlib.Path(sys.argv[1])
vercel = sys.argv[2] if len(sys.argv) > 2 else "https://<URL-DO-VERCEL>.vercel.app"
render = sys.argv[3] if len(sys.argv) > 3 else "https://<URL-DO-RENDER>.onrender.com"
repo = "https://github.com/GustavoLimaDias/Sistema-de-Moeda-Estudantil/tree/release-2"
placeholder = "<" in vercel or "<" in render

png = base64.b64encode((root / "docs/uml/release2/implantacao.png").read_bytes()).decode()

# Mirrors config/DataInitializer.java (password of every seeded user: 123456)
users = [
    ("Aluno", "Ana Souza", "ana.souza", "PUC Minas · Engenharia de Software"),
    ("Aluno", "Lucas Pereira", "lucas.pereira", "PUC Minas · Ciência da Computação"),
    ("Aluno", "Marina Costa", "marina.costa", "UFMG · Engenharia Civil"),
    ("Professor", "Carla Mendes", "carla.mendes", "PUC Minas · Engenharia de Software · saldo inicial 1.000 moedas"),
    ("Professor", "Ricardo Alves", "ricardo.alves", "PUC Minas · Ciência da Computação · saldo inicial 1.000 moedas"),
    ("Empresa parceira", "Cantina do Campus", "cantina", "Vantagens: Almoço com 20% de desconto (150) · Café com pão de queijo (50)"),
    ("Empresa parceira", "Papelaria Universitária", "papelaria", "Vantagem: Kit de materiais (300)"),
    ("Administrador", "Administrador", "admin", "Instituições, professores (importação), alunos, empresas e emails"),
]
rows = "".join(
    f"<tr><td>{html.escape(p)}</td><td>{html.escape(n)}</td><td><code>{html.escape(l)}</code></td>"
    f"<td><code>123456</code></td><td>{html.escape(o)}</td></tr>" for p, n, l, o in users)

e = html.escape
doc = f"""<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><title>Lab04S02 - Sistema de Moeda Estudantil</title>
<style>
@page {{ size: A4; margin: 16mm 15mm; }}
body {{ font-family: "Segoe UI", Helvetica, Arial, sans-serif; color: #1f2933; font-size: 10.5pt; line-height: 1.45; }}
header {{ border-bottom: 3px solid #2F5597; padding-bottom: 8px; margin-bottom: 14px; }}
header .inst {{ font-size: 9.5pt; color: #52606d; }}
h1 {{ font-size: 18pt; margin: 6px 0 2px; color: #2F5597; }}
h2 {{ font-size: 12.5pt; color: #2F5597; margin: 18px 0 6px; border-bottom: 1px solid #d9e2ec; padding-bottom: 2px; }}
.destaque {{ background: #EEF3FB; border: 2px solid #2F5597; border-radius: 6px; padding: 10px 14px; margin: 8px 0; }}
.destaque a.principal {{ font-size: 15pt; font-weight: 700; color: #2F5597; }}
a {{ color: #2F5597; }}
table {{ border-collapse: collapse; width: 100%; font-size: 9.5pt; }}
th, td {{ border: 1px solid #cbd2d9; padding: 5px 6px; text-align: left; vertical-align: top; }}
th {{ background: #2F5597; color: #fff; }}
tr:nth-child(even) td {{ background: #f5f7fa; }}
code {{ font-family: Consolas, monospace; font-size: 9.5pt; }}
.aviso {{ background: #FFFDE7; border-left: 4px solid #B8A100; padding: 6px 10px; }}
@page paisagem {{ size: A4 landscape; margin: 10mm; }}
.diagrama {{ page: paisagem; page-break-before: always; }}
.diagrama img {{ width: 100%; max-height: 160mm; object-fit: contain; border: 1px solid #d9e2ec; }}
.diagrama p {{ font-size: 9.5pt; margin: 4px 0 0; }}
</style></head><body>
<header>
<div class="inst">Pontifícia Universidade Católica de Minas Gerais · Engenharia de Software · Projeto de Software<br>
Profa. Milena Menezes Adão</div>
<h1>Laboratório 4 – Sprint 2 (Lab04S02) · Sistema de Moeda Estudantil</h1>
<div>Aluno: <b>Gustavo Lima Dias</b> · Data: {date.today().strftime('%d/%m/%Y')}</div>
</header>
{'<p class="aviso"><b>Rascunho:</b> URLs de produção ainda não definidas (placeholders).</p>' if placeholder else ''}
<h2>Links</h2>
<div class="destaque">Sistema implantado (front-end no Vercel):<br><a class="principal" href="{e(vercel)}">{e(vercel)}</a></div>
<p>API REST (back-end no Render): <a href="{e(render)}/api/health">{e(render)}/api/health</a><br>
Repositório (branch <code>release-2</code>): <a href="{repo}">{repo}</a></p>

<h2>Acessos (todos os usuários cadastrados)</h2>
<table><tr><th>Perfil</th><th>Nome</th><th>Login</th><th>Senha</th><th>Observação</th></tr>{rows}</table>
<p>Saldos iniciais (conforme a carga inicial): professores 1.000 moedas, alunos 0. No teste de produção de 18/11/2026,
Carla Mendes enviou 200 moedas a Ana Souza e Ana resgatou o almoço (150), por isso os saldos atuais podem diferir.</p>
<p>Novos alunos e empresas podem se cadastrar pela tela <i>Cadastro</i>. A senha pode ser trocada pelo botão <b>Senha</b> no topo.</p>

<h2>Roteiro rápido de teste</h2>
<ol>
<li>Entre como <code>carla.mendes</code> e envie moedas para Ana Souza, informando o motivo.</li>
<li>Entre como <code>ana.souza</code>: confira o saldo, o extrato e o email na aba <b>Emails</b>.</li>
<li>Ainda como aluna, resgate uma vantagem (ex.: Almoço com 20% de desconto) e anote o código do cupom.</li>
<li>Entre como <code>cantina</code>, abra <i>Resgates e cupons</i> e confira o código.</li>
</ol>

<h2>Observações</h2>
<ul>
<li>O back-end usa o plano gratuito do Render e "dorme" após 15 minutos sem uso: o <b>primeiro acesso pode levar cerca de 1 minuto</b>.</li>
<li>Os emails estão em <b>modo simulado</b> (o plano gratuito bloqueia SMTP): ficam gravados no banco e aparecem na aba <b>Emails</b> de cada usuário.</li>
<li>Sessões ficam em memória: depois que o servidor reinicia, é preciso fazer login de novo.</li>
</ul>

<div class="diagrama">
<h2>Diagrama de implantação</h2>
<img src="data:image/png;base64,{png}" alt="Diagrama de implantação UML">
<p>O navegador acessa só o domínio do Vercel, que entrega o front-end estático (HTML, CSS e JS) pela CDN e repassa
as chamadas <code>/api/*</code> por HTTPS ao Web Service do Render (rewrite em <code>vercel.json</code>). Assim não há
chamada cross-origin no navegador. No Render, um container Docker roda a JVM Eclipse Temurin 17 com o jar Spring Boot
(Tomcat embutido, perfil <code>prod</code>), que expõe a API REST com autenticação por token Bearer e acessa o PostgreSQL
gerenciado do Render via JDBC na rede privada. O health check <code>GET /api/health</code> é usado pelo Render e serve
para acordar o serviço. Um servidor SMTP externo é opcional e não é usado no plano gratuito.</p>
</div>
</body></html>"""

out_dir = root / "docs/entrega"
out_dir.mkdir(parents=True, exist_ok=True)
html_path = pathlib.Path(tempfile.gettempdir()) / "Lab04S02.html"
html_path.write_text(doc, encoding="utf-8")
pdf = out_dir / "Lab04S02_Moeda_Estudantil.pdf"
chrome = r"C:\Program Files\Google\Chrome\Application\chrome.exe"
subprocess.run([chrome, "--headless=new", "--disable-gpu", "--no-pdf-header-footer",
                f"--print-to-pdf={pdf}", html_path.as_uri()], check=True, timeout=120)
assert pdf.stat().st_size > 10_000, "PDF looks empty"
print(pdf)
