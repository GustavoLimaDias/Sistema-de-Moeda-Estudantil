package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.CreditoSemestral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditoSemestralRepository extends JpaRepository<CreditoSemestral, Long> {

    boolean existsByProfessorIdAndSemestre(Long professorId, String semestre);

    List<CreditoSemestral> findByProfessorIdOrderByDataHoraDesc(Long professorId);
}
