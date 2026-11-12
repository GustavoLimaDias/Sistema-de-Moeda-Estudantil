package br.pucminas.moedaestudantil.event;

/** Publicado ao concluir um resgate; dispara os emails de cupom (aluno) e de conferência (empresa). */
public record VantagemResgatadaEvent(Long resgateId) {
}
