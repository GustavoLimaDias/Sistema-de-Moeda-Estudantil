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
          <button class="btn fantasma pequeno" id="trocar-senha" title="Alterar senha">Senha</button>
          <button class="btn fantasma pequeno" id="sair">Sair</button>
        </div>
      </div>`;
    document.getElementById("trocar-senha").addEventListener("click", () => Layout.alterarSenha());
    document.getElementById("sair").addEventListener("click", async () => {
      try { await API.post("/api/auth/logout"); } catch (e) { /* sessão já encerrada */ }
      Sessao.limpar();
      window.location.href = "/index.html";
    });
  },

  /** UC15 – qualquer perfil troca a própria senha. */
  alterarSenha() {
    let d = document.getElementById("modal-senha");
    if (!d) {
      d = document.createElement("dialog");
      d.id = "modal-senha";
      d.className = "pequeno";
      d.innerHTML = `
        <form novalidate>
          <div class="modal-topo"><h2>Alterar senha</h2><button type="button" class="fechar" data-fechar aria-label="Fechar">×</button></div>
          <div class="modal-corpo">
            <div class="alerta" hidden></div>
            <div class="campo"><label for="senha-atual">Senha atual</label><input id="senha-atual" name="senhaAtual" type="password" autocomplete="current-password"><div class="msg-erro"></div></div>
            <div class="campo" style="margin-top:12px"><label for="senha-nova">Nova senha</label><input id="senha-nova" name="novaSenha" type="password" autocomplete="new-password"><div class="dica">Mínimo de 6 caracteres.</div><div class="msg-erro"></div></div>
          </div>
          <div class="modal-rodape"><button type="button" class="btn secundario" data-fechar>Cancelar</button><button type="submit" class="btn">Salvar</button></div>
        </form>`;
      document.body.appendChild(d);
      const form = d.querySelector("form");
      UI.limparAoEditar(form);
      d.querySelectorAll("[data-fechar]").forEach((b) => b.addEventListener("click", () => d.close()));
      form.addEventListener("submit", async (ev) => {
        ev.preventDefault();
        try {
          await API.put("/api/auth/senha", lerFormulario(form));
          d.close();
          UI.toast("Senha alterada.");
        } catch (e) {
          UI.mostrarErros(form, e);
        }
      });
    }
    const form = d.querySelector("form");
    form.reset();
    UI.limparErros(form);
    d.showModal();
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
