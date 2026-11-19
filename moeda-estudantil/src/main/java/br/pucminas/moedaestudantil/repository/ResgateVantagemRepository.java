package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.ResgateVantagem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResgateVantagemRepository extends JpaRepository<ResgateVantagem, Long> {

    @EntityGraph(attributePaths = {"vantagem", "vantagem.empresa"})
    List<ResgateVantagem> findByAlunoIdOrderByDataHoraDesc(Long alunoId);

    @EntityGraph(attributePaths = {"aluno", "vantagem"})
    List<ResgateVantagem> findByVantagemEmpresaIdOrderByDataHoraDesc(Long empresaId);

    @EntityGraph(attributePaths = {"aluno", "vantagem", "vantagem.empresa"})
    Optional<ResgateVantagem> findByCodigoCupom(String codigoCupom);

    @EntityGraph(attributePaths = {"aluno", "vantagem", "vantagem.empresa"})
    Optional<ResgateVantagem> findWithDetalhesById(Long id);

    /** Bloqueia o resgate para conferir ou cancelar sem que as duas operações aconteçam juntas. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ResgateVantagem r where r.codigoCupom = :codigo")
    Optional<ResgateVantagem> findParaAtualizarStatus(@Param("codigo") String codigo);

    boolean existsByCodigoCupom(String codigoCupom);

    boolean existsByVantagemId(Long vantagemId);

    boolean existsByVantagemEmpresaId(Long empresaId);

    boolean existsByAlunoId(Long alunoId);
}
