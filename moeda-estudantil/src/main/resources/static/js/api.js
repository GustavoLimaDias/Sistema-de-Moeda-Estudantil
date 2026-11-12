/* Camada de comunicação do front-end com a API REST do back-end. */
const API = (() => {
  async function requisicao(metodo, url, corpo) {
    const opcoes = { method: metodo, headers: { Accept: "application/json" } };
    if (corpo !== undefined) {
      opcoes.headers["Content-Type"] = "application/json";
      opcoes.body = JSON.stringify(corpo);
    }
    let resposta;
    try {
      resposta = await fetch(url, opcoes);
    } catch (e) {
      throw { status: 0, mensagem: "Não foi possível conectar ao servidor.", campos: {} };
    }
    if (resposta.status === 204) return null;
    const dados = await resposta.json().catch(() => null);
    if (!resposta.ok) {
      throw {
        status: resposta.status,
        mensagem: (dados && dados.mensagem) || "Erro inesperado (" + resposta.status + ").",
        campos: (dados && dados.campos) || {},
      };
    }
    return dados;
  }
  return {
    get: (url) => requisicao("GET", url),
    post: (url, corpo) => requisicao("POST", url, corpo),
    put: (url, corpo) => requisicao("PUT", url, corpo),
    del: (url) => requisicao("DELETE", url),
  };
})();

/* ---------- utilidades de interface ---------- */
const UI = {
  toast(texto, tipo = "ok") {
    let caixa = document.getElementById("toasts");
    if (!caixa) {
      caixa = document.createElement("div");
      caixa.id = "toasts";
      document.body.appendChild(caixa);
    }
    const t = document.createElement("div");
    t.className = "toast " + tipo;
    t.textContent = texto;
    caixa.appendChild(t);
    setTimeout(() => t.remove(), 3500);
  },

  escapar(valor) {
    return String(valor ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  },

  /** Remove o destaque de erro de um campo assim que o usuário volta a editá-lo. */
  limparAoEditar(form) {
    const limpar = (ev) => {
      const campo = ev.target.closest(".campo");
      if (!campo || !campo.classList.contains("invalido")) return;
      campo.classList.remove("invalido");
      const msg = campo.querySelector(".msg-erro");
      if (msg) msg.textContent = "";
    };
    form.addEventListener("input", limpar);
    form.addEventListener("change", limpar);
  },

  limparErros(form) {
    form.querySelectorAll(".campo.invalido").forEach((c) => c.classList.remove("invalido"));
    form.querySelectorAll(".msg-erro").forEach((m) => (m.textContent = ""));
    const alerta = form.querySelector(".alerta");
    if (alerta) alerta.hidden = true;
  },

  /** Mostra os erros devolvidos pela API ao lado de cada campo (campos com "endereco.uf" etc.). */
  mostrarErros(form, erro) {
    UI.limparErros(form);
    let algumCampo = false;
    Object.entries(erro.campos || {}).forEach(([campo, msg]) => {
      const input = form.querySelector(`[name="${campo}"]`);
      if (!input) return;
      algumCampo = true;
      const wrapper = input.closest(".campo");
      wrapper.classList.add("invalido");
      const alvo = wrapper.querySelector(".msg-erro");
      if (alvo) alvo.textContent = msg;
    });
    const alerta = form.querySelector(".alerta");
    if (alerta) {
      alerta.textContent = erro.mensagem;
      alerta.hidden = false;
    }
    if (!algumCampo && !alerta) UI.toast(erro.mensagem, "erro");
  },

  /** Confirmação sem window.confirm, usando o <dialog id="confirmar">. */
  confirmar(texto) {
    const d = document.getElementById("confirmar");
    d.querySelector("[data-texto]").textContent = texto;
    d.showModal();
    return new Promise((resolve) => {
      const fim = (v) => { d.close(); resolve(v); };
      d.querySelector("[data-sim]").onclick = () => fim(true);
      d.querySelector("[data-nao]").onclick = () => fim(false);
      d.oncancel = () => resolve(false);
    });
  },
};

/* ---------- máscaras e formatação ---------- */
const Formato = {
  digitos: (v) => String(v ?? "").replace(/\D/g, ""),
  cpf(v) {
    const d = Formato.digitos(v).slice(0, 11);
    return d.replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d{1,2})$/, "$1-$2");
  },
  cnpj(v) {
    const d = Formato.digitos(v).slice(0, 14);
    return d.replace(/^(\d{2})(\d)/, "$1.$2").replace(/^(\d{2})\.(\d{3})(\d)/, "$1.$2.$3")
      .replace(/\.(\d{3})(\d)/, ".$1/$2").replace(/(\d{4})(\d)/, "$1-$2");
  },
  cep(v) {
    const d = Formato.digitos(v).slice(0, 8);
    return d.replace(/(\d{5})(\d)/, "$1-$2");
  },
  aplicarMascaras(raiz) {
    raiz.querySelectorAll("[data-mascara]").forEach((input) => {
      const fn = Formato[input.dataset.mascara];
      input.addEventListener("input", () => { input.value = fn(input.value); });
    });
  },
};

/** Lê o formulário em objeto, montando objetos aninhados para nomes como "endereco.cidade". */
function lerFormulario(form) {
  const obj = {};
  new FormData(form).forEach((valor, chave) => {
    const partes = chave.split(".");
    let alvo = obj;
    partes.slice(0, -1).forEach((p) => { alvo = alvo[p] = alvo[p] || {}; });
    alvo[partes[partes.length - 1]] = typeof valor === "string" ? valor.trim() : valor;
  });
  return obj;
}

function preencherFormulario(form, dados, prefixo = "") {
  Object.entries(dados || {}).forEach(([chave, valor]) => {
    const nome = prefixo + chave;
    if (valor && typeof valor === "object" && !Array.isArray(valor)) {
      preencherFormulario(form, valor, nome + ".");
      return;
    }
    const input = form.querySelector(`[name="${nome}"]`);
    if (!input) return;
    if (input.type === "checkbox") input.checked = Boolean(valor);
    else input.value = input.dataset.mascara ? Formato[input.dataset.mascara](valor ?? "") : (valor ?? "");
  });
}
