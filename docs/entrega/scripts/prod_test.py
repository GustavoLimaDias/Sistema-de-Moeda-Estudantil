"""End-to-end production checks through the Vercel domain. Usage: python prod_test.py https://<vercel-domain>"""
import json
import sys
import urllib.error
import urllib.request

BASE = sys.argv[1].rstrip("/")
USERS = {"ana.souza": "ALUNO", "lucas.pereira": "ALUNO", "marina.costa": "ALUNO",
         "carla.mendes": "PROFESSOR", "ricardo.alves": "PROFESSOR",
         "cantina": "EMPRESA", "papelaria": "EMPRESA", "admin": "ADMIN"}
failures = []


def call(method, path, token=None, body=None, raw=False):
    headers = {"Accept": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    data = None
    if body is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(body).encode("utf-8")
    req = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=120) as r:
            content = r.read()
            return r.status, (content if raw else (json.loads(content) if content else None))
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", "replace")[:300]


def check(label, ok, detail=""):
    print(("PASS " if ok else "FAIL ") + label + (f"  [{detail}]" if detail else ""))
    if not ok:
        failures.append(label)


for path in ["/", "/index.html", "/cadastro.html", "/aluno.html", "/professor.html", "/empresa.html",
             "/admin-alunos.html", "/admin.html", "/admin-empresas.html", "/css/app.css", "/js/api.js",
             "/js/layout.js", "/img/sem-foto.svg", "/img/vantagem-almoco.svg"]:
    status, _ = call("GET", path, raw=True)
    check(f"GET {path} -> {status}", status == 200)

status, body = call("GET", "/api/health")
check(f"GET /api/health -> {status} {body}", status == 200 and body == {"status": "UP"})

tokens = {}
for login, perfil in USERS.items():
    status, body = call("POST", "/api/auth/login", body={"login": login, "senha": "123456"})
    ok = status == 200 and body.get("token") and body["usuario"]["perfil"] == perfil
    check(f"login {login} ({perfil}) -> {status}", bool(ok))
    if ok:
        tokens[login] = body["token"]

status, bad = call("POST", "/api/auth/login", body={"login": "ana.souza", "senha": "errada"})
check(f"login with wrong password rejected -> {status}", status in (401, 422))

carla, ana, cantina = tokens["carla.mendes"], tokens["ana.souza"], tokens["cantina"]
_, prof_before = call("GET", "/api/professores/me/extrato", carla)
_, ana_before = call("GET", "/api/alunos/me/extrato", ana)
check(f"professor extrato (saldo {prof_before['saldo']})", "saldo" in prof_before)
check(f"aluno extrato (saldo {ana_before['saldo']})", "saldo" in ana_before)

_, me = call("GET", "/api/auth/me", ana)
status, envio = call("POST", "/api/professores/me/envios", carla,
                     {"alunoId": me["id"], "valor": 200, "motivo": "Participação na aula (teste de produção)"})
check(f"professor envia 200 moedas -> {status}", status == 201)

_, ana_after = call("GET", "/api/alunos/me/extrato", ana)
first = ana_after["transacoes"][0]
check(f"aluno saldo {ana_before['saldo']} -> {ana_after['saldo']}, last={first['tipo']}",
      ana_after["saldo"] == ana_before["saldo"] + 200 and first["tipo"] == "RECEBIMENTO")
_, emails = call("GET", "/api/auth/emails", ana)
check(f"email simulado: {emails[0]['assunto']}", emails and "200 moedas" in emails[0]["assunto"])

_, catalogo = call("GET", "/api/vantagens", ana)
almoco = next(v for v in catalogo if v["nome"] == "Almoço com 20% de desconto")
status, resgate = call("POST", "/api/alunos/me/resgates", ana, {"vantagemId": almoco["id"]})
check(f"aluno resgata '{almoco['nome']}' -> {status}, cupom {resgate.get('codigoCupom') if status == 201 else resgate}",
      status == 201)
codigo = resgate["codigoCupom"]
_, emails = call("GET", "/api/auth/emails", ana)
check(f"email do cupom: {emails[0]['assunto']}", codigo in json.dumps(emails[0], ensure_ascii=False))

status, conf = call("POST", f"/api/empresas/me/resgates/{codigo}/conferir", cantina)
check(f"empresa confere cupom {codigo} -> {status} {conf.get('status') if isinstance(conf, dict) else conf}",
      status == 200 and conf["status"] == "CONFERIDO")

status, _ = call("GET", "/api/alunos/me/extrato")
check(f"extrato sem token -> {status}", status == 401)

print(f"\n{len(failures)} failure(s)")
sys.exit(1 if failures else 0)
