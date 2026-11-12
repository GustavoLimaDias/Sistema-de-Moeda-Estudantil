package br.pucminas.moedaestudantil.event;

/** Publicado ao concluir um envio de moedas; dispara o email ao aluno após o commit. */
public record MoedasEnviadasEvent(Long transferenciaId) {
}
