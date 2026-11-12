/* Administração: professores (UC14), instituições (UC13), crédito semestral (UC12) e emails. */
(() => {
  Layout.exigir("ADMIN");
  const erro = (e) => UI.toast(e.mensagem, "erro");
  const formProf = document.getElementById("form-prof");
  const formImport = document.getElementById("form-import");
  const formInst = document.getElementById("form-inst");
  let instituicoes = [];
  let editandoInst = null;

  // ---------- professores ----------
  async function carregarProfessores() {
    const lista = await API.get("/api/professores");
    document.getElementById("contagem-prof").textContent = `${lista.length} professor(es)`;
    document.getElementById("tabela-prof").innerHTML = lista.map((p) => `<tr>
      <td><strong>${UI.escapar(p.nome)}</strong><div class="sub">${UI.escapar(p.email)} · login ${UI.escapar(p.login)}</div></td>
      <td class="ocultar-mobile">${UI.escapar(p.instituicao.nome)}<div class="sub">${UI.escapar(p.departamento)}</div></td>
      <td><span class="selo">● ${p.saldo}</span></td></tr>`).join("");
  }

  formProf.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const dados = lerFormulario(formProf);
    dados.instituicaoId = dados.instituicaoId ? Number(dados.instituicaoId) : null;
    try {
      const p = await API.post("/api/professores", dados);
      UI.toast(`${p.nome} cadastrado com ${p.saldo} moedas.`);
      formProf.reset();
      UI.limparErros(formProf);
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
      <td><div class="acoes"><button class="btn secundario pequeno" data-editar-inst="${i.id}">Editar</button></div></td></tr>`).join("");
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

  document.getElementById("tabela-inst").addEventListener("click", (ev) => {
    const b = ev.target.closest("[data-editar-inst]");
    if (b) editarInstituicao(instituicoes.find((i) => String(i.id) === b.dataset.editarInst));
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
