package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Aluno;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    @EntityGraph(attributePaths = "instituicao")
    List<Aluno> findAllByOrderByNomeAsc();

    @EntityGraph(attributePaths = "instituicao")
    List<Aluno> findByInstituicaoIdOrderByNomeAsc(Long instituicaoId);

    @EntityGraph(attributePaths = "instituicao")
    Optional<Aluno> findWithInstituicaoById(Long id);

    /** Bloqueia a linha durante a transação para atualizar o saldo sem condição de corrida. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Aluno a where a.id = :id")
    Optional<Aluno> findParaAtualizarSaldo(@Param("id") Long id);
}
