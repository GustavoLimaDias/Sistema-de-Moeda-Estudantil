package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Professor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    boolean existsByCpf(String cpf);

    @EntityGraph(attributePaths = "instituicao")
    List<Professor> findAllByOrderByNomeAsc();

    @EntityGraph(attributePaths = "instituicao")
    Optional<Professor> findWithInstituicaoById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Professor p where p.id = :id")
    Optional<Professor> findParaAtualizarSaldo(@Param("id") Long id);
}
