package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/** Professor pré-cadastrado, obrigatoriamente vinculado a uma instituição. */
@Entity
@Table(name = "professor")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Professor extends Usuario {

    public static final int CREDITO_SEMESTRAL = 1000;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 100)
    private String departamento;

    @Column(nullable = false)
    private int saldo = 0;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private InstituicaoEnsino instituicao;

    /** O crédito semestral é acumulativo: soma ao saldo corrente. */
    public void creditarSemestre(int valor) {
        this.saldo += valor;
    }

    public void debitarMoedas(int valor) {
        if (valor > saldo) {
            throw new IllegalStateException("Saldo insuficiente.");
        }
        this.saldo -= valor;
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public int getSaldo() { return saldo; }
    public InstituicaoEnsino getInstituicao() { return instituicao; }
    public void setInstituicao(InstituicaoEnsino instituicao) { this.instituicao = instituicao; }
}
