package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

/** Usado pelo extrato e pelas operações de moeda (Sprint 3). */
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
}
