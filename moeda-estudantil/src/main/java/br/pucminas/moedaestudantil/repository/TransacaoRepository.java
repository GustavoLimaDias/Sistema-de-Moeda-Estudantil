package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso genérico à hierarquia de transações (consultas específicas ficam nos repositórios das subclasses). */
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
}
