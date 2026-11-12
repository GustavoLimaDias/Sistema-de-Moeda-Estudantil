/* Área do professor: envio de moedas (UC05) e extrato (UC04). */
(() => {
  Layout.exigir("PROFESSOR");
  const form = document.getElementById("form-envio");
  const select = document.getElementById("aluno");
  let saldo = 0;
  UI.limparAoEditar(form);

  async function carregarAlunos() {
    const alunos = await API.get("/api/alunos");
    document.getElementById("qtd-alunos").textContent = `${alunos.length} aluno(s) na sua instituição`;
    select.innerHTML = '<option value="">Selecione o aluno…</option>' + alunos.map((a) =>
      `<option value="${a.id}">${UI.escapar(a.nome)} — ${UI.escapar(a.curso)}</option>`).join("");
  }

  async function carregarExtrato() {
    const extrato = await API.get("/api/professores/me/extrato");
    saldo = extrato.saldo;
    const envios = extrato.transacoes.filter((t) => t.tipo === "ENVIO");
    document.getElementById("saldo").textContent = saldo;
    document.getElementById("enviadas").textContent = extrato.totalSaidas;
    document.getElementById("qtd-envios").textContent = `${envios.length} envio(s)`;
    document.getElementById("alunos-reconhecidos").textContent = new Set(envios.map((e) => e.contraparte)).size;
    document.getElementById("dica-valor").textContent = `Você pode enviar até ${saldo} moedas.`;
    Componentes.extrato(document.getElementById("extrato"), extrato, { rotuloContraparte: "Aluno" });
    document.getElementById("ultimos").innerHTML = envios.length
      ? envios.slice(0, 5).map((e) => `<li><div><strong>${UI.escapar(e.contraparte)}</strong><div class="sub">${UI.escapar(e.descricao)}</div></div><span class="selo">● ${-e.valor}</span></li>`).join("")
      : '<li class="sub">Nenhum envio ainda.</li>';
  }

  form.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const dados = lerFormulario(form);
    dados.alunoId = dados.alunoId ? Number(dados.alunoId) : null;
    dados.valor = dados.valor === "" ? null : Number(dados.valor);
    const botao = document.getElementById("enviar");
    botao.disabled = true;
    try {
      const t = await API.post("/api/professores/me/envios", dados);
      UI.toast(`${t.valor} moedas enviadas para ${t.alunoNome}.`);
      form.reset();
      UI.limparErros(form);
      await carregarExtrato();
    } catch (e) {
      UI.mostrarErros(form, e);
    } finally {
      botao.disabled = false;
    }
  });

  const erro = (e) => UI.toast(e.mensagem, "erro");
  Layout.abas();
  Promise.all([carregarAlunos(), carregarExtrato()]).catch(erro);
})();
