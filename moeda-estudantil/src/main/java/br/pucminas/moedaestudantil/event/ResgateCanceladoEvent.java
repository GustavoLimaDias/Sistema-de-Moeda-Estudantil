package br.pucminas.moedaestudantil.event;

import br.pucminas.moedaestudantil.model.Perfil;

/** Publicado ao cancelar um resgate; avisa aluno e empresa por email após o commit. */
public record ResgateCanceladoEvent(Long resgateId, Perfil canceladoPor) {
}
