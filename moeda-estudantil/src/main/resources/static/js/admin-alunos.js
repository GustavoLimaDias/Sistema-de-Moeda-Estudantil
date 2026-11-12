/* Administração: CRUD completo de alunos. */
(() => {
  Layout.exigir("ADMIN");
  const tabela = document.getElementById("tabela");
  const busca = document.getElementById("busca");
  const modal = document.getElementById("modal");
  const form = document.getElementById("form");
  form.querySelector("[data-campos]").innerHTML = Forms.aluno("f-");
  const preparado = Forms.preparar(form);
  modal.querySelectorAll("[data-fechar]").forEach((b) => b.addEventListener("click", () => modal.close()));

  let alunos = [];
  let editandoId = null;

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
    const digitos = Formato.digitos(termo);
    const lista = alunos.filter((a) => !termo ||
      [a.nome, a.email, a.curso, a.instituicao.nome].some((v) => v.toLowerCase().includes(termo)) ||
      (digitos && a.cpf.includes(digitos)));
    document.getElementById("contagem").textContent = lista.length + (lista.length === 1 ? " aluno" : " alunos");
    document.getElementById("vazio").hidden = lista.length > 0;
    tabela.innerHTML = lista.map((a) => `
      <tr>
        <td><strong>${UI.escapar(a.nome)}</strong><div class="sub">${UI.escapar(a.email)} · login ${UI.escapar(a.login)}</div></td>
        <td class="ocultar-mobile">${Formato.cpf(a.cpf)}</td>
        <td class="ocultar-mobile">${UI.escapar(a.instituicao.nome)}<div class="sub">${UI.escapar(a.curso)}</div></td>
        <td><span class="selo">● ${a.saldo}</span></td>
        <td><div class="acoes">
          <button class="btn secundario pequeno" data-editar="${a.id}">Editar</button>
          <button class="btn secundario pequeno" data-excluir="${a.id}">Excluir</button>
        </div></td>
      </tr>`).join("");
  }

  async function abrir(aluno) {
    await preparado;
    form.reset();
    UI.limparErros(form);
    editandoId = aluno ? aluno.id : null;
    document.getElementById("titulo").textContent = aluno ? "Editar aluno" : "Novo aluno";
    form.querySelector("[data-dica-senha]").textContent = aluno
      ? "Deixe em branco para manter a senha atual." : "Mínimo de 6 caracteres.";
    if (aluno) Forms.preencherAluno(form, aluno);
    modal.showModal();
  }

  form.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const botao = document.getElementById("salvar");
    botao.disabled = true;
    try {
      const dados = Forms.dadosAluno(form);
      if (editandoId) await API.put("/api/alunos/" + editandoId, dados);
      else await API.post("/api/alunos", dados);
      UI.toast(editandoId ? "Aluno atualizado." : "Aluno cadastrado.");
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
    try {
      if (editar) await abrir(await API.get("/api/alunos/" + editar.dataset.editar));
      if (excluir) {
        const a = alunos.find((x) => String(x.id) === excluir.dataset.excluir);
        if (!(await UI.confirmar(`Excluir o aluno "${a.nome}"? Esta ação não pode ser desfeita.`))) return;
        await API.del("/api/alunos/" + a.id);
        UI.toast("Aluno excluído.");
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
