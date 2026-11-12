/* Componentes de interface compartilhados entre os painéis. */
const TIPOS_EXTRATO = {
  RECEBIMENTO: ["Recebido", "verde"],
  RESGATE: ["Resgate", "laranja"],
  ENVIO: ["Enviado", "laranja"],
  CREDITO: ["Crédito semestral", "verde"],
};
const STATUS_CUPOM = { PENDENTE: ["Aguardando troca", ""], CONFERIDO: ["Utilizado", "verde"], CANCELADO: ["Cancelado", "cinza"] };

const Componentes = {
  extrato(alvo, extrato, { rotuloContraparte = "Origem/destino" } = {}) {
    if (!extrato.transacoes.length) {
      alvo.innerHTML = '<div class="vazio">Nenhuma movimentação ainda.</div>';
      return;
    }
    alvo.innerHTML = `<div class="tabela-wrap"><table>
      <thead><tr><th>Data</th><th>Tipo</th><th>Descrição</th><th class="ocultar-mobile">${rotuloContraparte}</th><th class="num">Moedas</th></tr></thead>
      <tbody>${extrato.transacoes.map((t) => {
        const [rotulo, cor] = TIPOS_EXTRATO[t.tipo] || [t.tipo, ""];
        const cupom = t.codigoCupom ? `<div class="sub">Cupom <code>${UI.escapar(t.codigoCupom)}</code></div>` : "";
        return `<tr>
          <td class="sub">${UI.dataHora(t.dataHora)}</td>
          <td><span class="selo ${cor}">${rotulo}</span></td>
          <td>${UI.escapar(t.descricao)}${cupom}</td>
          <td class="ocultar-mobile">${UI.escapar(t.contraparte)}</td>
          <td class="num ${t.valor > 0 ? "positivo" : "negativo"}">${t.valor > 0 ? "+" : "−"}${Math.abs(t.valor)}</td>
        </tr>`;
      }).join("")}</tbody></table></div>`;
  },

  emails(alvo, emails, { mostrarDestinatario = false } = {}) {
    if (!emails.length) {
      alvo.innerHTML = '<div class="vazio">Nenhum email recebido.</div>';
      return;
    }
    const statusRotulo = { ENVIADO: ["enviado por SMTP", "verde"], SIMULADO: ["simulado", "cinza"], FALHOU: ["falhou", "laranja"] };
    alvo.innerHTML = `<div class="emails">${emails.map((e) => {
      const [rot, cor] = statusRotulo[e.status] || [e.status, ""];
      return `<details class="email">
        <summary>
          <span class="assunto">${UI.escapar(e.assunto)}</span>
          <span class="sub">${mostrarDestinatario ? UI.escapar(e.destinatario) + " · " : ""}${UI.dataHora(e.dataHora)}</span>
          <span class="selo ${cor}">${rot}</span>
        </summary>
        <pre>${UI.escapar(e.corpo)}</pre>
      </details>`;
    }).join("")}</div>`;
  },

  statusCupom(status) {
    const [rotulo, cor] = STATUS_CUPOM[status] || [status, ""];
    return `<span class="selo ${cor}">${rotulo}</span>`;
  },
};
