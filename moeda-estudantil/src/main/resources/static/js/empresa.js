/* Área da empresa parceira: vantagens (UC11), conferência de cupons, emails e dados cadastrais. */
(() => {
  const usuario = Layout.exigir("EMPRESA");
  const erro = (e) => UI.toast(e.mensagem, "erro");

  async function atualizarResumoVantagens() {
    const vantagens = await API.get(`/api/empresas/${usuario.id}/vantagens`);
    document.getElementById("ativas").textContent = vantagens.filter((v) => v.ativa).length;
    document.getElementById("total-vantagens").textContent = `${vantagens.length} cadastrada(s)`;
  }

  const editor = EditorVantagens(document.getElementById("editor"), {
    aoMudar: () => atualizarResumoVantagens().catch(erro),
  });

  // ---------- resgates ----------
  async function carregarResgates() {
    const resgates = await API.get("/api/empresas/me/resgates");
    const conferidos = resgates.filter((r) => r.status === "CONFERIDO");
    document.getElementById("pendentes").textContent = resgates.filter((r) => r.status === "PENDENTE").length;
    document.getElementById("conferidos").textContent = conferidos.length;
    document.getElementById("moedas-trocadas").textContent =
      `${conferidos.reduce((s, r) => s + r.custo, 0)} moedas em vantagens entregues`;
    const alvo = document.getElementById("resgates");
    if (!resgates.length) {
      alvo.innerHTML = '<div class="vazio">Nenhuma vantagem resgatada ainda.</div>';
      return;
    }
    alvo.innerHTML = `<div class="tabela-wrap"><table>
      <thead><tr><th>Data</th><th>Aluno</th><th>Vantagem</th><th>Cupom</th><th>Situação</th><th></th></tr></thead>
      <tbody>${resgates.map((r) => `<tr>
        <td class="sub">${UI.dataHora(r.dataHora)}</td>
        <td><strong>${UI.escapar(r.alunoNome)}</strong><div class="sub">${UI.escapar(r.alunoEmail)}</div></td>
        <td>${UI.escapar(r.vantagemNome)}<div class="sub">${r.custo} moedas</div></td>
        <td><code>${UI.escapar(r.codigoCupom)}</code></td>
        <td>${Componentes.statusCupom(r.status)}</td>
        <td><div class="acoes">${r.status === "PENDENTE" ? `<button class="btn secundario pequeno" data-conferir="${r.codigoCupom}">Marcar como usado</button><button class="btn secundario pequeno" data-cancelar="${r.codigoCupom}">Cancelar</button>` : ""}</div></td>
      </tr>`).join("")}</tbody></table></div>`;
  }

  async function conferir(codigo) {
    const r = await API.post(`/api/empresas/me/resgates/${encodeURIComponent(codigo)}/conferir`);
    UI.toast(`Cupom de ${r.alunoNome} conferido: ${r.vantagemNome}.`);
    await carregarResgates();
  }

  const formConferir = document.getElementById("form-conferir");
  UI.limparAoEditar(formConferir);
  formConferir.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const codigo = formConferir.codigo.value.trim();
    if (!codigo) {
      UI.mostrarErros(formConferir, { mensagem: "", campos: { codigo: "Digite o código do cupom." } });
      return;
    }
    try {
      await conferir(codigo);
      formConferir.reset();
      UI.limparErros(formConferir);
    } catch (e) {
      UI.mostrarErros(formConferir, { ...e, campos: { codigo: e.mensagem } });
    }
  });
  document.getElementById("resgates").addEventListener("click", async (ev) => {
    const c = ev.target.closest("[data-cancelar]");
    if (c) {
      if (!(await UI.confirmar(`Cancelar o cupom ${c.dataset.cancelar}? O aluno recebe as moedas de volta e é avisado por email.`, "Cancelar cupom"))) return;
      try {
        await API.post(`/api/empresas/me/resgates/${encodeURIComponent(c.dataset.cancelar)}/cancelar`);
        UI.toast("Cupom cancelado e moedas devolvidas ao aluno.");
        await carregarResgates();
      } catch (e) {
        erro(e);
      }
      return;
    }
    const b = ev.target.closest("[data-conferir]");
    if (!b) return;
    if (!(await UI.confirmar(`Confirmar a troca do cupom ${b.dataset.conferir}?`, "Confirmar"))) return;
    conferir(b.dataset.conferir).catch(erro);
  });

  // ---------- perfil ----------
  const formPerfil = document.getElementById("form-perfil");
  formPerfil.querySelector("[data-campos]").innerHTML =
    Forms.empresa("p-", { dicaSenha: "Deixe em branco para manter a senha atual." });
  async function carregarPerfil() {
    await Forms.preparar(formPerfil);
    preencherFormulario(formPerfil, { ...(await API.get("/api/empresas/" + usuario.id)), senha: "" });
  }
  formPerfil.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    try {
      const atualizada = await API.put("/api/empresas/" + usuario.id, lerFormulario(formPerfil));
      Sessao.salvar({ ...Sessao.get(), usuario: { ...usuario, nome: atualizada.nome, email: atualizada.email } });
      Layout.topo(Sessao.usuario());
      formPerfil.querySelector("[name=senha]").value = "";
      UI.limparErros(formPerfil);
      UI.toast("Dados atualizados.");
    } catch (e) {
      UI.mostrarErros(formPerfil, e);
    }
  });
  document.getElementById("excluir-conta").addEventListener("click", async () => {
    if (!(await UI.confirmar("Excluir a conta da empresa e todas as suas vantagens?"))) return;
    try {
      await API.del("/api/empresas/" + usuario.id);
      Sessao.limpar();
      window.location.href = "/index.html";
    } catch (e) {
      erro(e);
    }
  });

  Layout.abas((aba) => {
    if (aba === "resgates") carregarResgates().catch(erro);
    if (aba === "emails") API.get("/api/auth/emails")
      .then((l) => Componentes.emails(document.getElementById("emails"), l)).catch(erro);
    if (aba === "perfil" && !formPerfil.dataset.carregado) {
      formPerfil.dataset.carregado = "1";
      carregarPerfil().catch(erro);
    }
  });
  Promise.all([editor.abrirEmpresa(usuario.id), atualizarResumoVantagens(), carregarResgates()]).catch(erro);
})();
