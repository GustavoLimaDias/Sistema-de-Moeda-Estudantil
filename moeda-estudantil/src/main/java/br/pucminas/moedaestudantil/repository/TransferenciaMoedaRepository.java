package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.TransferenciaMoeda;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransferenciaMoedaRepository extends JpaRepository<TransferenciaMoeda, Long> {

    @EntityGraph(attributePaths = "remetente")
    List<TransferenciaMoeda> findByDestinatarioIdOrderByDataHoraDesc(Long alunoId);

    @EntityGraph(attributePaths = "destinatario")
    List<TransferenciaMoeda> findByRemetenteIdOrderByDataHoraDesc(Long professorId);

    @EntityGraph(attributePaths = {"remetente", "destinatario"})
    Optional<TransferenciaMoeda> findWithParticipantesById(Long id);

    boolean existsByDestinatarioId(Long alunoId);

    boolean existsByRemetenteId(Long professorId);
}
