/* CRUD de empresas parceiras e de suas vantagens via /api/empresas. */
(() => {
  const tabela = document.getElementById("tabela");
  const vazio = document.getElementById("vazio");
  const contagem = document.getElementById("contagem");
  const busca = document.getElementById("busca");

  const modal = document.getElementById("modal");
  const form = document.getElementById("form");
  const modalVantagens = document.getElementById("modal-vantagens");
  const modalVantagem = document.getElementById("modal-vantagem");
  const formVantagem = document.getElementById("form-vantagem");
  const listaVantagens = document.getElementById("lista-vantagens");

  let empresas = [];
  let editandoId = null;
  let empresaAtual = null;      // empresa cujas vantagens estão abertas
  let vantagens = [];
  let editandoVantagemId = null;

  Formato.aplicarMascaras(form);
  UI.limparAoEditar(form);
  UI.limparAoEditar(formVantagem);
  [modal, modalVantagens, modalVantagem].forEach((m) =>
    m.querySelectorAll("[data-fechar]").forEach((b) => b.addEventListener("click", () => m.close())));

  // ---------------- empresas ----------------

  async function carregar() {
    try {
      empresas = await API.get("/api/empresas");
      renderizar();
    } catch (e) {
      UI.toast(e.mensagem, "erro");
    }
  }

  function renderizar() {
    const termo = busca.value.trim().toLowerCase();
    const termoDigitos = Formato.digitos(termo);
    const lista = empresas.filter((e) => !termo ||
      [e.nome, e.email].some((v) => v.toLowerCase().includes(termo)) ||
      (termoDigitos && e.cnpj.includes(termoDigitos)));

    contagem.textContent = lista.length + (lista.length === 1 ? " empresa" : " empresas");
    vazio.hidden = lista.length > 0;
    tabela.innerHTML = lista.map((e) => `
      <tr>
        <td><strong>${UI.escapar(e.nome)}</strong><div class="sub">${UI.escapar(e.email)}</div></td>
        <td class="ocultar-mobile">${Formato.cnpj(e.cnpj)}</td>
        <td><button class="btn secundario pequeno" data-vantagens="${e.id}">${e.quantidadeVantagens} ${e.quantidadeVantagens === 1 ? "vantagem" : "vantagens"}</button></td>
        <td><div class="acoes">
          <button class="btn secundario pequeno" data-editar="${e.id}">Editar</button>
          <button class="btn secundario pequeno" data-excluir="${e.id}">Excluir</button>
        </div></td>
      </tr>`).join("");
  }

  function abrir(empresa) {
    form.reset();
    UI.limparErros(form);
    editandoId = empresa ? empresa.id : null;
    document.getElementById("titulo").textContent = empresa ? "Editar empresa" : "Nova empresa";
    document.getElementById("dica-senha").textContent = empresa
      ? "Deixe em branco para manter a senha atual." : "Mínimo de 6 caracteres.";
    if (empresa) preencherFormulario(form, { ...empresa, senha: "" });
    modal.showModal();
    document.getElementById("nome").focus();
  }

  form.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const dados = lerFormulario(form);
    const botao = document.getElementById("salvar");
    botao.disabled = true;
    try {
      if (editandoId) {
        await API.put("/api/empresas/" + editandoId, dados);
        UI.toast("Empresa atualizada.");
      } else {
        await API.post("/api/empresas", dados);
        UI.toast("Empresa cadastrada.");
      }
      modal.close();
      carregar();
    } catch (e) {
      UI.mostrarErros(form, e);
    } finally {
      botao.disabled = false;
    }
  });

  tabela.addEventListener("click", async (ev) => {
    const editar = ev.target.closest("[data-editar]");
    const excluir = ev.target.closest("[data-excluir]");
    const verVantagens = ev.target.closest("[data-vantagens]");
    try {
      if (editar) abrir(await API.get("/api/empresas/" + editar.dataset.editar));
      if (verVantagens) abrirVantagens(empresas.find((e) => String(e.id) === verVantagens.dataset.vantagens));
      if (excluir) {
        const empresa = empresas.find((e) => String(e.id) === excluir.dataset.excluir);
        const aviso = empresa.quantidadeVantagens > 0
          ? ` As ${empresa.quantidadeVantagens} vantagens dela também serão removidas.` : "";
        if (!(await UI.confirmar(`Excluir a empresa "${empresa.nome}"?${aviso}`))) return;
        await API.del("/api/empresas/" + empresa.id);
        UI.toast("Empresa excluída.");
        carregar();
      }
    } catch (e) {
      UI.toast(e.mensagem, "erro");
    }
  });

  // ---------------- vantagens ----------------

  async function abrirVantagens(empresa) {
    empresaAtual = empresa;
    document.getElementById("titulo-vantagens").textContent = "Vantagens · " + empresa.nome;
    listaVantagens.innerHTML = "";
    if (!modalVantagens.open) modalVantagens.showModal();
    await carregarVantagens();
  }

  async function carregarVantagens() {
    try {
      vantagens = await API.get(`/api/empresas/${empresaAtual.id}/vantagens`);
    } catch (e) {
      UI.toast(e.mensagem, "erro");
      return;
    }
    const ativas = vantagens.filter((v) => v.ativa).length;
    document.getElementById("resumo-vantagens").textContent =
      `${vantagens.length} cadastrada(s) · ${ativas} ativa(s)`;
    document.getElementById("vazio-vantagens").hidden = vantagens.length > 0;
    listaVantagens.innerHTML = vantagens.map((v) => `
      <article class="vantagem${v.ativa ? "" : " inativa"}">
        <img src="${UI.escapar(v.fotoUrl)}" alt="${UI.escapar(v.nome)}" onerror="this.src='/img/sem-foto.svg'">
        <div class="info">
          <h4>${UI.escapar(v.nome)}</h4>
          <p>${UI.escapar(v.descricao)}</p>
          <div class="rodape">
            <span class="selo">● ${v.custo}</span>
            ${v.ativa ? "" : '<span class="selo cinza">inativa</span>'}
          </div>
          <div class="acoes">
            <button class="btn secundario pequeno" data-editar-v="${v.id}">Editar</button>
            <button class="btn secundario pequeno" data-excluir-v="${v.id}">Excluir</button>
          </div>
        </div>
      </article>`).join("");
  }

  function abrirVantagem(vantagem) {
    formVantagem.reset();
    UI.limparErros(formVantagem);
    editandoVantagemId = vantagem ? vantagem.id : null;
    document.getElementById("titulo-vantagem").textContent = vantagem ? "Editar vantagem" : "Nova vantagem";
    document.getElementById("v-ativa").checked = true;
    if (vantagem) preencherFormulario(formVantagem, vantagem);
    modalVantagem.showModal();
  }

  formVantagem.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const dados = lerFormulario(formVantagem);
    dados.custo = dados.custo === "" ? null : Number(dados.custo);
    dados.ativa = document.getElementById("v-ativa").checked;
    const base = `/api/empresas/${empresaAtual.id}/vantagens`;
    try {
      if (editandoVantagemId) {
        await API.put(`${base}/${editandoVantagemId}`, dados);
        UI.toast("Vantagem atualizada.");
      } else {
        await API.post(base, dados);
        UI.toast("Vantagem cadastrada.");
      }
      modalVantagem.close();
      await carregarVantagens();
      carregar();
    } catch (e) {
      UI.mostrarErros(formVantagem, e);
    }
  });

  listaVantagens.addEventListener("click", async (ev) => {
    const editar = ev.target.closest("[data-editar-v]");
    const excluir = ev.target.closest("[data-excluir-v]");
    if (editar) abrirVantagem(vantagens.find((v) => String(v.id) === editar.dataset.editarV));
    if (excluir) {
      const v = vantagens.find((x) => String(x.id) === excluir.dataset.excluirV);
      if (!(await UI.confirmar(`Excluir a vantagem "${v.nome}"?`))) return;
      try {
        await API.del(`/api/empresas/${empresaAtual.id}/vantagens/${v.id}`);
        UI.toast("Vantagem excluída.");
        await carregarVantagens();
        carregar();
      } catch (e) {
        UI.toast(e.mensagem, "erro");
      }
    }
  });

  document.getElementById("nova-vantagem").addEventListener("click", () => abrirVantagem(null));
  document.getElementById("novo").addEventListener("click", () => abrir(null));
  busca.addEventListener("input", renderizar);

  carregar();
})();
