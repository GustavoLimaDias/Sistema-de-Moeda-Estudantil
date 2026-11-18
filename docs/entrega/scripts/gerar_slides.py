"""Builds docs/apresentacao/Lab04S02_Slides.pdf (16:9, HTML -> PDF via headless Chrome).
Usage: python docs/entrega/scripts/gerar_slides.py .
"""
import base64
import pathlib
import subprocess
import sys
import tempfile

root = pathlib.Path(sys.argv[1]).resolve()
VERCEL = "https://moeda-estudantil-phi.vercel.app"
RENDER = "https://moeda-estudantil-api-zj8w.onrender.com"
REPO = "https://github.com/GustavoLimaDias/Sistema-de-Moeda-Estudantil/tree/release-2"
png = base64.b64encode((root / "docs/uml/release2/implantacao.png").read_bytes()).decode()

slides = [
    f"""<div class="capa"><div class="tag">PUC Minas · Engenharia de Software · Projeto de Software</div>
    <h1>Sistema de Moeda Estudantil</h1><h2>Lab04S02 · Implantação na nuvem</h2>
    <p>Gustavo Lima Dias · Profa. Milena Menezes Adão</p>
    <p class="link"><a href="{VERCEL}">{VERCEL.replace('https://', '')}</a></p></div>""",

    """<h1>Arquitetura de implantação</h1><div class="cols">
    <div class="card"><h3>Vercel (CDN)</h3><p>Front-end estático: HTML, CSS e JS.<br>
    <code>vercel.json</code> repassa <code>/api/*</code> ao Render, então o navegador fala com um único domínio.</p></div>
    <div class="card"><h3>Render · Web Service</h3><p>Container Docker com JVM Temurin 17 e o jar Spring Boot 3.3.5 (perfil <code>prod</code>).<br>
    Health check <code>GET /api/health</code>.</p></div>
    <div class="card"><h3>Render · PostgreSQL</h3><p>Banco <code>moeda_estudantil</code> via JDBC na rede privada.<br>
    Acesso externo bloqueado (<code>ipAllowList: []</code>).</p></div></div>
    <p class="rodape">Emails em modo simulado: o plano gratuito do Render bloqueia SMTP (aba <b>Emails</b>).</p>""",

    f"""<h1>Diagrama de implantação (UML)</h1><img class="diag" src="data:image/png;base64,{png}">""",

    """<h1>O que mudou no código</h1><ul>
    <li><b>Perfil <code>prod</code></b>: porta <code>PORT</code>, PostgreSQL por variáveis de ambiente, pool Hikari de 5 conexões.</li>
    <li><b>CORS configurável</b> (<code>CORS_ALLOWED_ORIGINS</code>), fixado na origem exata do Vercel.</li>
    <li><b><code>/api/health</code></b> público, para o health check e para acordar o serviço.</li>
    <li><b>Dockerfile multi-stage</b> (Maven → JRE 17, <code>MaxRAMPercentage=75</code> para 512 MB).</li>
    <li><b><code>render.yaml</code></b> (Blueprint) e <b><code>vercel.json</code></b> (rewrite da API).</li>
    <li>Regras de negócio, entidades, endpoints e telas <b>não mudaram</b>; 15 testes automatizados passando.</li></ul>""",

    f"""<h1>Acesso e demonstração</h1><div class="cols"><div>
    <table><tr><th>Perfil</th><th>Login</th></tr>
    <tr><td>Aluno</td><td><code>ana.souza</code> · <code>lucas.pereira</code> · <code>marina.costa</code></td></tr>
    <tr><td>Professor</td><td><code>carla.mendes</code> · <code>ricardo.alves</code></td></tr>
    <tr><td>Empresa</td><td><code>cantina</code> · <code>papelaria</code></td></tr>
    <tr><td>Admin</td><td><code>admin</code></td></tr></table>
    <p>Senha de todos: <code>123456</code></p></div>
    <div><ol><li>Professor envia moedas com motivo</li><li>Aluno vê o extrato e o email</li>
    <li>Aluno resgata uma vantagem (cupom)</li><li>Empresa confere o cupom</li></ol>
    <p class="aviso">Primeiro acesso: cerca de 1 min (Render gratuito "dorme" após 15 min).</p></div></div>
    <p class="rodape">Sistema: <a href="{VERCEL}">{VERCEL}</a><br>API: {RENDER}/api/health<br>Repositório: {REPO}</p>""",
]

doc = """<!doctype html><html lang="pt-BR"><head><meta charset="utf-8"><title>Lab04S02 Slides</title><style>
@page { size: 338.67mm 190.5mm; margin: 0; }
* { box-sizing: border-box; }
body { margin: 0; font-family: "Segoe UI", Helvetica, Arial, sans-serif; color: #1f2933; }
section { width: 338.67mm; height: 190.5mm; padding: 16mm 20mm; page-break-after: always; overflow: hidden;
  position: relative; border-top: 6mm solid #2F5597; }
section:last-child { page-break-after: auto; }
h1 { color: #2F5597; font-size: 30pt; margin: 0 0 10mm; }
h3 { color: #2F5597; margin: 0 0 3mm; font-size: 17pt; }
p, li, td, th { font-size: 16pt; line-height: 1.45; }
li { margin-bottom: 3mm; }
code { font-family: Consolas, monospace; background: #EEF3FB; padding: 0 3px; border-radius: 3px; }
a { color: #2F5597; }
.capa { display: flex; flex-direction: column; justify-content: center; height: 100%; }
.capa h1 { font-size: 42pt; margin: 4mm 0; } .capa h2 { font-size: 24pt; color: #52606d; margin: 0 0 8mm; font-weight: 500; }
.tag { color: #52606d; font-size: 14pt; } .link { font-size: 22pt; font-weight: 700; }
.cols { display: grid; grid-template-columns: repeat(auto-fit, minmax(0, 1fr)); gap: 10mm; }
.card { background: #EEF3FB; border: 1px solid #2F5597; border-radius: 4mm; padding: 6mm; }
.card p { font-size: 14pt; }
table { border-collapse: collapse; width: 100%; } th, td { border: 1px solid #cbd2d9; padding: 2mm 3mm; text-align: left; font-size: 14pt; }
th { background: #2F5597; color: #fff; }
.aviso { background: #FFFDE7; border-left: 4px solid #B8A100; padding: 3mm 5mm; font-size: 14pt; }
.rodape { position: absolute; bottom: 10mm; left: 20mm; right: 20mm; font-size: 12pt; color: #52606d; margin: 0; }
.diag { display: block; max-width: 100%; max-height: 140mm; margin: 0 auto; }
</style></head><body>""" + "".join(f"<section>{s}</section>" for s in slides) + "</body></html>"

html_path = pathlib.Path(tempfile.gettempdir()) / "Lab04S02_Slides.html"
html_path.write_text(doc, encoding="utf-8")
out = root / "docs/apresentacao/Lab04S02_Slides.pdf"
subprocess.run([r"C:\Program Files\Google\Chrome\Application\chrome.exe", "--headless=new", "--disable-gpu",
                "--no-pdf-header-footer", f"--print-to-pdf={out}", html_path.as_uri()], check=True, timeout=120)
assert out.stat().st_size > 10_000, "PDF looks empty"
print(out)
