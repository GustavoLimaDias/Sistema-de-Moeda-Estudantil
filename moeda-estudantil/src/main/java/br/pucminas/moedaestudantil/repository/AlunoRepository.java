package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Aluno;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    @EntityGraph(attributePaths = "instituicao")
    List<Aluno> findAllByOrderByNomeAsc();

    @EntityGraph(attributePaths = "instituicao")
    Optional<Aluno> findWithInstituicaoById(Long id);
}
