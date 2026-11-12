/* Área do aluno: catálogo e resgate (UC07/UC08), extrato (UC04), cupons, emails e perfil (UC15). */
(() => {
  const usuario = Layout.exigir("ALUNO");
  let saldo = 0;
  let vantagens = [];
  let extrato = null;

  const catalogo = document.getElementById("catalogo");
  const busca = document.getElementById("busca");
  const soPosso = document.getElementById("so-posso");
  const modalCupom = document.getElementById("modal-cupom");
  modalCupom.querySelectorAll("[data-fechar]").forEach((b) => b.addEventListener("click", () => modalCupom.close()));

  async function carregarExtrato() {
    extrato = await API.get("/api/alunos/me/extrato");
    saldo = extrato.saldo;
    const recebimentos = extrato.transacoes.filter((t) => t.tipo === "RECEBIMENTO");
    const resgates = extrato.transacoes.filter((t) => t.tipo === "RESGATE");
    const pendentes = resgates.filter((t) => t.status === "PENDENTE");
    document.getElementById("saldo").textContent = saldo;
    document.getElementById("recebidas").textContent = extrato.totalEntradas;
    document.getElementById("qtd-reconhecimentos").textContent =
      recebimentos.length === 1 ? "1 reconhecimento" : `${recebimentos.length} reconhecimentos`;
    document.getElementById("cupons-ativos").textContent = pendentes.length;
    document.getElementById("qtd-trocas").textContent = `${resgates.length} troca(s) no total`;
    Componentes.extrato(document.getElementById("extrato"), extrato, { rotuloContraparte: "Professor / empresa" });
    renderizarCupons(resgates);
    renderizarCatalogo();
  }

  function renderizarCupons(resgates) {
    const alvo = document.getElementById("cupons");
    if (!resgates.length) {
      alvo.innerHTML = '<div class="vazio cartao" style="grid-column:1/-1">Você ainda não resgatou nenhuma vantagem.</div>';
      return;
    }
    alvo.innerHTML = resgates.map((r) => `
      <article class="cartao cartao-cupom">
        <div style="display:flex;justify-content:space-between;gap:8px;align-items:center">
          <strong>${UI.escapar(r.descricao)}</strong>${Componentes.statusCupom(r.status)}
        </div>
        <div class="sub">${UI.escapar(r.contraparte)} · ${UI.dataHora(r.dataHora)} · ${Math.abs(r.valor)} moedas</div>
        <div class="codigo">${UI.escapar(r.codigoCupom)}</div>
      </article>`).join("");
  }

  async function carregarCatalogo() {
    vantagens = await API.get("/api/vantagens");
    renderizarCatalogo();
  }

  function renderizarCatalogo() {
    const termo = busca.value.trim().toLowerCase();
    const lista = vantagens.filter((v) =>
      (!termo || v.nome.toLowerCase().includes(termo) || v.empresaNome.toLowerCase().includes(termo)) &&
      (!soPosso.checked || v.custo <= saldo));
    document.getElementById("catalogo-vazio").hidden = lista.length > 0;
    catalogo.innerHTML = lista.map((v) => {
      const falta = v.custo - saldo;
      return `
      <article class="vantagem">
        <img src="${UI.escapar(v.fotoUrl)}" alt="${UI.escapar(v.nome)}" onerror="this.src='/img/sem-foto.svg'">
        <div class="info">
          <span class="empresa">${UI.escapar(v.empresaNome)}</span>
          <h4>${UI.escapar(v.nome)}</h4>
          <p>${UI.escapar(v.descricao)}</p>
          <div class="rodape">
            <span class="preco">● ${v.custo}</span>
            ${falta > 0
              ? `<span class="falta">faltam ${falta} moedas</span>`
              : `<button class="btn pequeno" data-resgatar="${v.id}">Resgatar</button>`}
          </div>
        </div>
      </article>`;
    }).join("");
  }

  catalogo.addEventListener("click", async (ev) => {
    const botao = ev.target.closest("[data-resgatar]");
    if (!botao) return;
    const v = vantagens.find((x) => String(x.id) === botao.dataset.resgatar);
    const ok = await UI.confirmar(`Trocar ${v.custo} moedas por "${v.nome}" (${v.empresaNome})? Seu saldo ficará em ${saldo - v.custo}.`, "Resgatar");
    if (!ok) return;
    botao.disabled = true;
    try {
      const resgate = await API.post("/api/alunos/me/resgates", { vantagemId: v.id });
      document.getElementById("cupom-vantagem").textContent = resgate.vantagemNome;
      document.getElementById("cupom-empresa").textContent = resgate.empresaNome;
      document.getElementById("cupom-codigo").textContent = resgate.codigoCupom;
      modalCupom.showModal();
      await carregarExtrato();
    } catch (e) {
      UI.toast(e.mensagem, "erro");
      botao.disabled = false;
    }
  });
  busca.addEventListener("input", renderizarCatalogo);
  soPosso.addEventListener("change", renderizarCatalogo);

  // ---------- perfil ----------
  const formPerfil = document.getElementById("form-perfil");
  formPerfil.querySelector("[data-campos]").innerHTML =
    Forms.aluno("p-", { dicaSenha: "Deixe em branco para manter a senha atual." });

  async function carregarPerfil() {
    await Forms.preparar(formPerfil);
    Forms.preencherAluno(formPerfil, await API.get("/api/alunos/" + usuario.id));
  }

  formPerfil.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    try {
      const atualizado = await API.put("/api/alunos/" + usuario.id, Forms.dadosAluno(formPerfil));
      Sessao.salvar({ ...Sessao.get(), usuario: { ...usuario, nome: atualizado.nome, email: atualizado.email } });
      Layout.topo(Sessao.usuario());
      formPerfil.querySelector("[name=senha]").value = "";
      UI.limparErros(formPerfil);
      UI.toast("Perfil atualizado.");
    } catch (e) {
      UI.mostrarErros(formPerfil, e);
    }
  });

  document.getElementById("excluir-conta").addEventListener("click", async () => {
    if (!(await UI.confirmar("Excluir sua conta? Esta ação não pode ser desfeita."))) return;
    try {
      await API.del("/api/alunos/" + usuario.id);
      Sessao.limpar();
      window.location.href = "/index.html";
    } catch (e) {
      UI.toast(e.mensagem, "erro");
    }
  });

  // ---------- emails ----------
  async function carregarEmails() {
    Componentes.emails(document.getElementById("emails"), await API.get("/api/auth/emails"));
  }

  const erro = (e) => UI.toast(e.mensagem, "erro");
  Layout.abas((aba) => {
    if (aba === "emails") carregarEmails().catch(erro);
    if (aba === "perfil" && !formPerfil.dataset.carregado) {
      formPerfil.dataset.carregado = "1";
      carregarPerfil().catch(erro);
    }
  });
  Promise.all([carregarExtrato(), carregarCatalogo()]).catch(erro);
})();
