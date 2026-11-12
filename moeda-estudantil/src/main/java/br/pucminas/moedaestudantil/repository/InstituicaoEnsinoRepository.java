package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstituicaoEnsinoRepository extends JpaRepository<InstituicaoEnsino, Long> {

    List<InstituicaoEnsino> findAllByOrderByNomeAsc();

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
