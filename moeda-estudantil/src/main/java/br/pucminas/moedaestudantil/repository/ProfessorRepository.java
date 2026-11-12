package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    boolean existsByCpf(String cpf);
}
