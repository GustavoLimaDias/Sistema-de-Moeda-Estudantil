/* Cabeçalho, controle de acesso das páginas e abas. */
const PAINEIS = {
  ALUNO: "/aluno.html",
  PROFESSOR: "/professor.html",
  EMPRESA: "/empresa.html",
  ADMIN: "/admin-alunos.html",
};
const NOMES_PERFIL = { ALUNO: "Aluno", PROFESSOR: "Professor", EMPRESA: "Empresa parceira", ADMIN: "Administrador" };
const MENUS = {
  ADMIN: [["/admin-alunos.html", "Alunos"], ["/admin-empresas.html", "Empresas"], ["/admin.html", "Professores e instituições"]],
};

/* Interrompe o script da página após redirecionar, sem poluir o console. */
class Redirecionando extends Error {}
window.addEventListener("error", (e) => { if (e.error instanceof Redirecionando) e.preventDefault(); });

const Layout = {
  /** Garante que há um usuário logado com um dos perfis; senão volta ao login. Retorna o usuário. */
  exigir(...perfis) {
    const usuario = Sessao.usuario();
    if (!usuario) {
      window.location.replace("/index.html");
      throw new Redirecionando("sem sessão");
    }
    if (perfis.length && !perfis.includes(usuario.perfil)) {
      window.location.replace(PAINEIS[usuario.perfil]);
      throw new Redirecionando("perfil sem acesso");
    }
    Layout.topo(usuario);
    return usuario;
  },

  topo(usuario) {
    const atual = window.location.pathname;
    const links = (MENUS[usuario.perfil] || [])
      .map(([href, rotulo]) => `<a href="${href}"${atual === href ? ' class="ativo"' : ""}>${rotulo}</a>`).join("");
    const iniciais = usuario.nome.split(" ").filter(Boolean).slice(0, 2).map((p) => p[0]).join("").toUpperCase();
    const el = document.getElementById("topo");
    el.className = "topo";
    el.innerHTML = `
      <div class="conteudo">
        <a class="marca" href="${PAINEIS[usuario.perfil]}"><span class="moeda">M</span><span class="nome">Moeda Estudantil</span></a>
        <nav class="menu">${links}</nav>
        <div class="usuario">
          <span class="avatar">${UI.escapar(iniciais)}</span>
          <span class="quem"><strong>${UI.escapar(usuario.nome)}</strong><small>${NOMES_PERFIL[usuario.perfil]}</small></span>
          <button class="btn fantasma pequeno" id="sair">Sair</button>
        </div>
      </div>`;
    document.getElementById("sair").addEventListener("click", async () => {
      try { await API.post("/api/auth/logout"); } catch (e) { /* sessão já encerrada */ }
      Sessao.limpar();
      window.location.href = "/index.html";
    });
  },

  /** Abas simples: botões [data-aba="x"] mostram a seção #aba-x; lembra a aba no hash da URL. */
  abas(aoTrocar) {
    const botoes = [...document.querySelectorAll("[data-aba]")];
    const mostrar = (nome) => {
      botoes.forEach((b) => b.classList.toggle("ativa", b.dataset.aba === nome));
      document.querySelectorAll(".aba").forEach((s) => (s.hidden = s.id !== "aba-" + nome));
      history.replaceState(null, "", "#" + nome);
      if (aoTrocar) aoTrocar(nome);
    };
    botoes.forEach((b) => b.addEventListener("click", () => mostrar(b.dataset.aba)));
    const inicial = location.hash.slice(1);
    mostrar(botoes.some((b) => b.dataset.aba === inicial) ? inicial : botoes[0].dataset.aba);
    return mostrar;
  },
};
