/* CRUD de alunos: lista, cadastro, edição e exclusão via /api/alunos. */
(() => {
  const tabela = document.getElementById("tabela");
  const vazio = document.getElementById("vazio");
  const contagem = document.getElementById("contagem");
  const busca = document.getElementById("busca");
  const modal = document.getElementById("modal");
  const form = document.getElementById("form");
  const selectInst = document.getElementById("instituicaoId");

  let alunos = [];
  let editandoId = null;

  Formato.aplicarMascaras(form);
  UI.limparAoEditar(form);
  modal.querySelectorAll("[data-fechar]").forEach((b) => b.addEventListener("click", () => modal.close()));

  async function carregarInstituicoes() {
    const insts = await API.get("/api/instituicoes");
    selectInst.innerHTML = '<option value="">Selecione…</option>' +
      insts.map((i) => `<option value="${i.id}">${UI.escapar(i.nome)}</option>`).join("");
  }

  async function carregar() {
    try {
      alunos = await API.get("/api/alunos");
      renderizar();
    } catch (e) {
      UI.toast(e.mensagem, "erro");
    }
  }

  function renderizar() {
    const termo = busca.value.trim().toLowerCase();
    const termoDigitos = Formato.digitos(termo);
    const lista = alunos.filter((a) => !termo ||
      [a.nome, a.email, a.curso, a.instituicao.nome].some((v) => v.toLowerCase().includes(termo)) ||
      (termoDigitos && a.cpf.includes(termoDigitos)));

    contagem.textContent = lista.length + (lista.length === 1 ? " aluno" : " alunos");
    vazio.hidden = lista.length > 0;
    tabela.innerHTML = lista.map((a) => `
      <tr>
        <td><strong>${UI.escapar(a.nome)}</strong><div class="sub">${UI.escapar(a.email)}</div></td>
        <td class="ocultar-mobile">${Formato.cpf(a.cpf)}</td>
        <td class="ocultar-mobile">${UI.escapar(a.instituicao.nome)}<div class="sub">${UI.escapar(a.curso)}</div></td>
        <td><span class="selo">● ${a.saldo}</span></td>
        <td><div class="acoes">
          <button class="btn secundario pequeno" data-editar="${a.id}">Editar</button>
          <button class="btn secundario pequeno" data-excluir="${a.id}">Excluir</button>
        </div></td>
      </tr>`).join("");
  }

  function abrir(aluno) {
    form.reset();
    UI.limparErros(form);
    editandoId = aluno ? aluno.id : null;
    document.getElementById("titulo").textContent = aluno ? "Editar aluno" : "Novo aluno";
    document.getElementById("dica-senha").textContent = aluno
      ? "Deixe em branco para manter a senha atual." : "Mínimo de 6 caracteres.";
    if (aluno) {
      preencherFormulario(form, { ...aluno, instituicaoId: aluno.instituicao.id, instituicao: undefined, senha: "" });
    }
    modal.showModal();
    document.getElementById("nome").focus();
  }

  form.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const dados = lerFormulario(form);
    dados.instituicaoId = dados.instituicaoId ? Number(dados.instituicaoId) : null;
    dados.endereco.uf = (dados.endereco.uf || "").toUpperCase();

    const botao = document.getElementById("salvar");
    botao.disabled = true;
    try {
      if (editandoId) {
        await API.put("/api/alunos/" + editandoId, dados);
        UI.toast("Aluno atualizado.");
      } else {
        await API.post("/api/alunos", dados);
        UI.toast("Aluno cadastrado.");
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
    if (editar) {
      try {
        abrir(await API.get("/api/alunos/" + editar.dataset.editar));
      } catch (e) {
        UI.toast(e.mensagem, "erro");
      }
    }
    if (excluir) {
      const aluno = alunos.find((a) => String(a.id) === excluir.dataset.excluir);
      if (!(await UI.confirmar(`Excluir o aluno "${aluno.nome}"? Esta ação não pode ser desfeita.`))) return;
      try {
        await API.del("/api/alunos/" + aluno.id);
        UI.toast("Aluno excluído.");
        carregar();
      } catch (e) {
        UI.toast(e.mensagem, "erro");
      }
    }
  });

  document.getElementById("novo").addEventListener("click", () => abrir(null));
  busca.addEventListener("input", renderizar);

  carregarInstituicoes().catch((e) => UI.toast(e.mensagem, "erro"));
  carregar();
})();
