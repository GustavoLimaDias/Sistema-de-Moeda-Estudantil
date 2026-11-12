/* Blocos de formulário reaproveitados no cadastro público, no perfil e na administração. */
const campo = (id, nome, rotulo, classe, extra = "") =>
  `<div class="campo ${classe}"><label for="${id}">${rotulo}</label><input id="${id}" name="${nome}" ${extra}><div class="msg-erro"></div></div>`;

const Forms = {
  aluno(p, { comSenha = true, dicaSenha = "Mínimo de 6 caracteres." } = {}) {
    return `
      <fieldset><legend>Dados pessoais</legend><div class="grade">
        ${campo(p + "nome", "nome", "Nome completo", "c4", 'maxlength="120"')}
        ${campo(p + "email", "email", "Email", "c2", 'type="email" maxlength="120"')}
        ${campo(p + "cpf", "cpf", "CPF", "c2", 'data-mascara="cpf" placeholder="000.000.000-00"')}
        ${campo(p + "rg", "rg", "RG", "c2", 'maxlength="20"')}
        <div class="campo c2"><label for="${p}inst">Instituição de ensino</label><select id="${p}inst" name="instituicaoId" data-instituicoes></select><div class="msg-erro"></div></div>
        ${campo(p + "curso", "curso", "Curso", "c6", 'maxlength="100"')}
      </div></fieldset>
      <fieldset><legend>Endereço</legend><div class="grade">
        ${campo(p + "cep", "endereco.cep", "CEP", "c2", 'data-mascara="cep" placeholder="00000-000"')}
        ${campo(p + "logradouro", "endereco.logradouro", "Logradouro", "c4", 'maxlength="150"')}
        ${campo(p + "numero", "endereco.numero", "Número", "c1", 'maxlength="10"')}
        ${campo(p + "complemento", "endereco.complemento", "Complemento", "c2", 'maxlength="60"')}
        ${campo(p + "bairro", "endereco.bairro", "Bairro", "c3", 'maxlength="80"')}
        ${campo(p + "cidade", "endereco.cidade", "Cidade", "c4", 'maxlength="80"')}
        ${campo(p + "uf", "endereco.uf", "UF", "c2", 'maxlength="2" style="text-transform:uppercase"')}
      </div></fieldset>
      ${Forms.acesso(p, comSenha, dicaSenha)}`;
  },

  empresa(p, { comSenha = true, dicaSenha = "Mínimo de 6 caracteres." } = {}) {
    return `
      <fieldset><legend>Dados da empresa</legend><div class="grade">
        ${campo(p + "nome", "nome", "Nome", "c4", 'maxlength="120"')}
        ${campo(p + "cnpj", "cnpj", "CNPJ", "c2", 'data-mascara="cnpj" placeholder="00.000.000/0000-00"')}
        ${campo(p + "email", "email", "Email", "c6", 'type="email" maxlength="120"')}
        <div class="campo c6"><label for="${p}descricao">Descrição</label><textarea id="${p}descricao" name="descricao" maxlength="500"></textarea><div class="msg-erro"></div></div>
      </div></fieldset>
      ${Forms.acesso(p, comSenha, dicaSenha)}`;
  },

  acesso(p, comSenha, dicaSenha) {
    return `<fieldset><legend>Acesso</legend><div class="grade">
        ${campo(p + "login", "login", "Login", "c3", 'maxlength="60" autocomplete="off"')}
        ${comSenha ? `<div class="campo c3"><label for="${p}senha">Senha</label><input id="${p}senha" name="senha" type="password" autocomplete="new-password"><div class="dica" data-dica-senha>${dicaSenha}</div><div class="msg-erro"></div></div>` : ""}
      </div></fieldset>`;
  },

  /** Prepara um <form>: máscaras, limpeza de erros e lista de instituições. */
  async preparar(form) {
    Formato.aplicarMascaras(form);
    UI.limparAoEditar(form);
    const select = form.querySelector("[data-instituicoes]");
    if (select) {
      const insts = await API.get("/api/instituicoes");
      select.innerHTML = '<option value="">Selecione…</option>' +
        insts.map((i) => `<option value="${i.id}">${UI.escapar(i.nome)}</option>`).join("");
    }
  },

  dadosAluno(form) {
    const dados = lerFormulario(form);
    dados.instituicaoId = dados.instituicaoId ? Number(dados.instituicaoId) : null;
    dados.endereco.uf = (dados.endereco.uf || "").toUpperCase();
    return dados;
  },

  preencherAluno(form, aluno) {
    preencherFormulario(form, { ...aluno, instituicaoId: aluno.instituicao.id, instituicao: undefined, senha: "" });
  },
};
