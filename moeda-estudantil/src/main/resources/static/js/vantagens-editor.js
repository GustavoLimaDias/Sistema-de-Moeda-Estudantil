/* Cadastro de vantagens de uma empresa (UC11), usado no painel da empresa e na administração. */
function EditorVantagens(alvo, { aoMudar } = {}) {
  let empresaId = null;
  let vantagens = [];
  let editandoId = null;

  alvo.innerHTML = `
    <div class="topo-bloco">
      <span class="sub" data-resumo></span>
      <button type="button" class="btn pequeno" data-nova>+ Nova vantagem</button>
    </div>
    <div class="vantagens" data-lista></div>
    <div class="vazio" data-vazio hidden>Nenhuma vantagem cadastrada ainda.</div>`;

  const modal = document.createElement("dialog");
  modal.style.width = "min(560px, calc(100vw - 32px))";
  modal.innerHTML = `
    <form novalidate>
      <div class="modal-topo"><h2 data-titulo>Nova vantagem</h2><button type="button" class="fechar" data-fechar aria-label="Fechar">×</button></div>
      <div class="modal-corpo">
        <div class="alerta" hidden></div>
        <div class="grade">
          <div class="campo c4"><label>Nome</label><input name="nome" maxlength="120"><div class="msg-erro"></div></div>
          <div class="campo c2"><label>Custo (moedas)</label><input name="custo" type="number" min="1" step="1"><div class="msg-erro"></div></div>
          <div class="campo c6"><label>Descrição</label><textarea name="descricao" maxlength="1000"></textarea><div class="msg-erro"></div></div>
          <div class="campo c6"><label>URL da foto do produto</label><input name="fotoUrl" maxlength="500" placeholder="https://… ou /img/…"><div class="msg-erro"></div></div>
          <div class="campo c6 previa"><img data-previa alt="" hidden></div>
          <div class="campo c6"><label class="check"><input type="checkbox" name="ativa"> Vantagem ativa (visível para os alunos)</label></div>
        </div>
      </div>
      <div class="modal-rodape"><button type="button" class="btn secundario" data-fechar>Cancelar</button><button type="submit" class="btn">Salvar</button></div>
    </form>`;
  document.body.appendChild(modal);
  const form = modal.querySelector("form");
  UI.limparAoEditar(form);
  modal.querySelectorAll("[data-fechar]").forEach((b) => b.addEventListener("click", () => modal.close()));
  const previa = modal.querySelector("[data-previa]");
  const atualizarPrevia = () => {
    const url = form.fotoUrl.value.trim();
    previa.hidden = !url;
    if (url) previa.src = url;
  };
  previa.onerror = () => { previa.src = "/img/sem-foto.svg"; };
  form.fotoUrl.addEventListener("input", atualizarPrevia);

  const base = () => `/api/empresas/${empresaId}/vantagens`;

  async function carregar() {
    try {
      vantagens = await API.get(base());
    } catch (e) {
      UI.toast(e.mensagem, "erro");
      return;
    }
    const ativas = vantagens.filter((v) => v.ativa).length;
    alvo.querySelector("[data-resumo]").textContent = `${vantagens.length} cadastrada(s) · ${ativas} ativa(s)`;
    alvo.querySelector("[data-vazio]").hidden = vantagens.length > 0;
    alvo.querySelector("[data-lista]").innerHTML = vantagens.map((v) => `
      <article class="vantagem${v.ativa ? "" : " inativa"}">
        <img src="${UI.escapar(v.fotoUrl)}" alt="${UI.escapar(v.nome)}" onerror="this.src='/img/sem-foto.svg'">
        <div class="info">
          <h4>${UI.escapar(v.nome)}</h4>
          <p>${UI.escapar(v.descricao)}</p>
          <div class="rodape"><span class="selo">● ${v.custo}</span>${v.ativa ? "" : '<span class="selo cinza">inativa</span>'}</div>
          <div class="acoes">
            <button class="btn secundario pequeno" data-editar-v="${v.id}">Editar</button>
            <button class="btn secundario pequeno" data-excluir-v="${v.id}">Excluir</button>
          </div>
        </div>
      </article>`).join("");
  }

  function abrir(v) {
    form.reset();
    UI.limparErros(form);
    editandoId = v ? v.id : null;
    modal.querySelector("[data-titulo]").textContent = v ? "Editar vantagem" : "Nova vantagem";
    form.ativa.checked = true;
    if (v) preencherFormulario(form, v);
    atualizarPrevia();
    modal.showModal();
  }

  form.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const dados = lerFormulario(form);
    dados.custo = dados.custo === "" ? null : Number(dados.custo);
    dados.ativa = form.ativa.checked;
    try {
      if (editandoId) await API.put(`${base()}/${editandoId}`, dados);
      else await API.post(base(), dados);
      UI.toast(editandoId ? "Vantagem atualizada." : "Vantagem cadastrada.");
      modal.close();
      await carregar();
      if (aoMudar) aoMudar();
    } catch (e) {
      UI.mostrarErros(form, e);
    }
  });

  alvo.querySelector("[data-nova]").addEventListener("click", () => abrir(null));
  alvo.querySelector("[data-lista]").addEventListener("click", async (ev) => {
    const editar = ev.target.closest("[data-editar-v]");
    const excluir = ev.target.closest("[data-excluir-v]");
    if (editar) abrir(vantagens.find((v) => String(v.id) === editar.dataset.editarV));
    if (excluir) {
      const v = vantagens.find((x) => String(x.id) === excluir.dataset.excluirV);
      if (!(await UI.confirmar(`Excluir a vantagem "${v.nome}"?`))) return;
      try {
        await API.del(`${base()}/${v.id}`);
        UI.toast("Vantagem excluída.");
        await carregar();
        if (aoMudar) aoMudar();
      } catch (e) {
        UI.toast(e.mensagem, "erro");
      }
    }
  });

  return {
    async abrirEmpresa(id) {
      empresaId = id;
      alvo.querySelector("[data-lista]").innerHTML = "";
      await carregar();
    },
  };
}
