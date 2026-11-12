package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Vantagem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VantagemRepository extends JpaRepository<Vantagem, Long> {

    List<Vantagem> findByEmpresaIdOrderByNomeAsc(Long empresaId);

    Optional<Vantagem> findByIdAndEmpresaId(Long id, Long empresaId);

    long countByEmpresaId(Long empresaId);

    /** Catálogo exibido aos alunos. */
    @EntityGraph(attributePaths = "empresa")
    List<Vantagem> findByAtivaTrueOrderByCustoAsc();

    @EntityGraph(attributePaths = "empresa")
    Optional<Vantagem> findWithEmpresaById(Long id);
}
