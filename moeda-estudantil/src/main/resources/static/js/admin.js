/* Administração: professores (UC14), instituições (UC13), crédito semestral (UC12) e emails. */
(() => {
  Layout.exigir("ADMIN");
  const erro = (e) => UI.toast(e.mensagem, "erro");
  const formProf = document.getElementById("form-prof");
  const formImport = document.getElementById("form-import");
  const formInst = document.getElementById("form-inst");
  let instituicoes = [];
  let professores = [];
  let editandoInst = null;
  let editandoProf = null;

  // ---------- professores ----------
  async function carregarProfessores() {
    professores = await API.get("/api/professores");
    document.getElementById("contagem-prof").textContent = `${professores.length} professor(es)`;
    document.getElementById("tabela-prof").innerHTML = professores.map((p) => `<tr>
      <td><strong>${UI.escapar(p.nome)}</strong><div class="sub">${UI.escapar(p.email)} · login ${UI.escapar(p.login)}</div></td>
      <td class="ocultar-mobile">${UI.escapar(p.instituicao.nome)}<div class="sub">${UI.escapar(p.departamento)}</div></td>
      <td><span class="selo">● ${p.saldo}</span></td>
      <td><div class="acoes">
        <button class="btn secundario pequeno" data-editar-prof="${p.id}">Editar</button>
        <button class="btn secundario pequeno" data-excluir-prof="${p.id}">Excluir</button>
      </div></td></tr>`).join("");
  }

  function editarProfessor(p) {
    editandoProf = p ? p.id : null;
    formProf.reset();
    UI.limparErros(formProf);
    document.getElementById("titulo-prof").textContent = p ? "Editar professor" : "Cadastrar professor";
    document.getElementById("dica-prof").textContent = p
      ? "Alterações de nome, email, CPF, departamento ou instituição."
      : "Sem senha, a senha inicial é o CPF (só números).";
    document.getElementById("salvar-prof").textContent = p ? "Salvar" : "Cadastrar";
    document.getElementById("cancelar-prof").hidden = !p;
    if (p) {
      preencherFormulario(formProf, { ...p, instituicaoId: p.instituicao.id, instituicao: undefined });
      formProf.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  }

  document.getElementById("cancelar-prof").addEventListener("click", () => editarProfessor(null));
  document.getElementById("tabela-prof").addEventListener("click", async (ev) => {
    const ed = ev.target.closest("[data-editar-prof]");
    const ex = ev.target.closest("[data-excluir-prof]");
    if (ed) editarProfessor(professores.find((p) => String(p.id) === ed.dataset.editarProf));
    if (ex) {
      const p = professores.find((x) => String(x.id) === ex.dataset.excluirProf);
      if (!(await UI.confirmar(`Excluir o professor "${p.nome}"?`))) return;
      try {
        await API.del("/api/professores/" + p.id);
        UI.toast("Professor excluído.");
        if (editandoProf === p.id) editarProfessor(null);
        carregarProfessores();
      } catch (e) {
        erro(e);
      }
    }
  });

  formProf.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const dados = lerFormulario(formProf);
    dados.instituicaoId = dados.instituicaoId ? Number(dados.instituicaoId) : null;
    try {
      if (editandoProf) {
        await API.put("/api/professores/" + editandoProf, dados);
        UI.toast("Professor atualizado.");
      } else {
        const p = await API.post("/api/professores", dados);
        UI.toast(`${p.nome} cadastrado com ${p.saldo} moedas.`);
      }
      editarProfessor(null);
      carregarProfessores();
    } catch (e) {
      UI.mostrarErros(formProf, e);
    }
  });

  formImport.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    UI.limparErros(formImport);
    const instituicaoId = Number(formImport.instituicaoId.value) || null;
    const linhas = formImport.lista.value.split("\n").map((l) => l.trim()).filter(Boolean);
    if (!instituicaoId || !linhas.length) {
      UI.mostrarErros(formImport, { mensagem: "Selecione a instituição e cole a lista.", campos: {} });
      return;
    }
    const lista = linhas.map((l) => {
      const [nome, email, cpf, departamento, login] = l.split(/[;\t]/).map((c) => (c || "").trim());
      return { nome, email, cpf, departamento, login, instituicaoId };
    });
    try {
      const criados = await API.post("/api/professores/importacao", lista);
      UI.toast(`${criados.length} professor(es) importado(s).`);
      formImport.reset();
      UI.limparErros(formImport);
      carregarProfessores();
    } catch (e) {
      UI.mostrarErros(formImport, e);
    }
  });

  // ---------- instituições ----------
  async function carregarInstituicoes() {
    instituicoes = await API.get("/api/instituicoes");
    document.getElementById("tabela-inst").innerHTML = instituicoes.map((i) => `<tr>
      <td><strong>${UI.escapar(i.nome)}</strong></td><td>${Formato.cnpj(i.cnpj)}</td>
      <td><div class="acoes"><button class="btn secundario pequeno" data-editar-inst="${i.id}">Editar</button><button class="btn secundario pequeno" data-excluir-inst="${i.id}">Excluir</button></div></td></tr>`).join("");
    const opcoes = '<option value="">Selecione…</option>' +
      instituicoes.map((i) => `<option value="${i.id}">${UI.escapar(i.nome)}</option>`).join("");
    document.querySelectorAll("[data-instituicoes]").forEach((s) => {
      const atual = s.value;
      s.innerHTML = opcoes;
      s.value = atual;
    });
  }

  function editarInstituicao(inst) {
    editandoInst = inst ? inst.id : null;
    formInst.reset();
    UI.limparErros(formInst);
    document.getElementById("titulo-inst").textContent = inst ? "Editar instituição" : "Nova instituição";
    document.getElementById("cancelar-inst").hidden = !inst;
    if (inst) preencherFormulario(formInst, inst);
  }

  document.getElementById("tabela-inst").addEventListener("click", async (ev) => {
    const b = ev.target.closest("[data-editar-inst]");
    if (b) editarInstituicao(instituicoes.find((i) => String(i.id) === b.dataset.editarInst));
    const x = ev.target.closest("[data-excluir-inst]");
    if (x) {
      const i = instituicoes.find((y) => String(y.id) === x.dataset.excluirInst);
      if (!(await UI.confirmar(`Excluir a instituição "${i.nome}"?`))) return;
      try {
        await API.del("/api/instituicoes/" + i.id);
        UI.toast("Instituição excluída.");
        if (editandoInst === i.id) editarInstituicao(null);
        carregarInstituicoes();
      } catch (e) {
        erro(e);
      }
    }
  });
  document.getElementById("cancelar-inst").addEventListener("click", () => editarInstituicao(null));
  formInst.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    try {
      const dados = lerFormulario(formInst);
      if (editandoInst) await API.put("/api/instituicoes/" + editandoInst, dados);
      else await API.post("/api/instituicoes", dados);
      UI.toast("Instituição salva.");
      editarInstituicao(null);
      carregarInstituicoes();
    } catch (e) {
      UI.mostrarErros(formInst, e);
    }
  });

  // ---------- crédito semestral ----------
  document.getElementById("creditar").addEventListener("click", async () => {
    try {
      const r = await API.post("/api/admin/creditos-semestrais");
      const caixa = document.getElementById("resultado-credito");
      caixa.hidden = false;
      caixa.textContent = r.professoresCreditados
        ? `Semestre ${r.semestre}: ${r.professoresCreditados} professor(es) receberam ${r.valorPorProfessor} moedas.`
        : `Semestre ${r.semestre}: todos os professores já tinham recebido o crédito.`;
      carregarProfessores();
    } catch (e) {
      erro(e);
    }
  });

  [formProf, formImport, formInst].forEach((f) => { Formato.aplicarMascaras(f); UI.limparAoEditar(f); });
  Layout.abas((aba) => {
    if (aba === "emails") API.get("/api/admin/emails")
      .then((l) => Componentes.emails(document.getElementById("emails"), l, { mostrarDestinatario: true })).catch(erro);
  });
  Promise.all([carregarProfessores(), carregarInstituicoes()]).catch(erro);
})();
