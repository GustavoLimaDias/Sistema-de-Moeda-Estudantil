/* Administração: CRUD completo de empresas parceiras e de suas vantagens. */
(() => {
  Layout.exigir("ADMIN");
  const tabela = document.getElementById("tabela");
  const busca = document.getElementById("busca");
  const modal = document.getElementById("modal");
  const form = document.getElementById("form");
  const modalVantagens = document.getElementById("modal-vantagens");
  form.querySelector("[data-campos]").innerHTML = Forms.empresa("f-");
  Forms.preparar(form);
  [modal, modalVantagens].forEach((m) =>
    m.querySelectorAll("[data-fechar]").forEach((b) => b.addEventListener("click", () => m.close())));
  const editor = EditorVantagens(document.getElementById("editor"), { aoMudar: () => carregar() });

  let empresas = [];
  let editandoId = null;

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
    const digitos = Formato.digitos(termo);
    const lista = empresas.filter((e) => !termo ||
      [e.nome, e.email].some((v) => v.toLowerCase().includes(termo)) || (digitos && e.cnpj.includes(digitos)));
    document.getElementById("contagem").textContent = lista.length + (lista.length === 1 ? " empresa" : " empresas");
    document.getElementById("vazio").hidden = lista.length > 0;
    tabela.innerHTML = lista.map((e) => `
      <tr>
        <td><strong>${UI.escapar(e.nome)}</strong><div class="sub">${UI.escapar(e.email)} · login ${UI.escapar(e.login)}</div></td>
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
    form.querySelector("[data-dica-senha]").textContent = empresa
      ? "Deixe em branco para manter a senha atual." : "Mínimo de 6 caracteres.";
    if (empresa) preencherFormulario(form, { ...empresa, senha: "" });
    modal.showModal();
  }

  form.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const botao = document.getElementById("salvar");
    botao.disabled = true;
    try {
      const dados = lerFormulario(form);
      if (editandoId) await API.put("/api/empresas/" + editandoId, dados);
      else await API.post("/api/empresas", dados);
      UI.toast(editandoId ? "Empresa atualizada." : "Empresa cadastrada.");
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
    const ver = ev.target.closest("[data-vantagens]");
    try {
      if (editar) abrir(await API.get("/api/empresas/" + editar.dataset.editar));
      if (ver) {
        const e = empresas.find((x) => String(x.id) === ver.dataset.vantagens);
        document.getElementById("titulo-vantagens").textContent = "Vantagens · " + e.nome;
        modalVantagens.showModal();
        await editor.abrirEmpresa(e.id);
      }
      if (excluir) {
        const e = empresas.find((x) => String(x.id) === excluir.dataset.excluir);
        const aviso = e.quantidadeVantagens > 0 ? ` As ${e.quantidadeVantagens} vantagens dela também serão removidas.` : "";
        if (!(await UI.confirmar(`Excluir a empresa "${e.nome}"?${aviso}`))) return;
        await API.del("/api/empresas/" + e.id);
        UI.toast("Empresa excluída.");
        carregar();
      }
    } catch (e) {
      UI.toast(e.mensagem, "erro");
    }
  });

  document.getElementById("novo").addEventListener("click", () => abrir(null));
  busca.addEventListener("input", renderizar);
  carregar();
})();
