package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.ResgateVantagem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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

    boolean existsByCodigoCupom(String codigoCupom);

    boolean existsByVantagemId(Long vantagemId);

    boolean existsByVantagemEmpresaId(Long empresaId);

    boolean existsByAlunoId(Long alunoId);
}
