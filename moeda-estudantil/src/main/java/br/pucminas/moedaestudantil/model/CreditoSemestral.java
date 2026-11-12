package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Crédito de 1.000 moedas ao professor no início de cada semestre (um por semestre). */
@Entity
@Table(name = "credito_semestral",
        uniqueConstraints = @UniqueConstraint(name = "uk_credito_professor_semestre",
                columnNames = {"professor_id", "semestre"}))
@PrimaryKeyJoinColumn(name = "transacao_id")
public class CreditoSemestral extends Transacao {

    /** Formato "2026/2". */
    @Column(nullable = false, length = 6)
    private String semestre;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @Override
    public String getDescricao() {
        return "Crédito semestral " + semestre;
    }

    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
}
