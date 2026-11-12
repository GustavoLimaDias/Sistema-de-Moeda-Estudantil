package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.UUID;

/** Troca de moedas do aluno por uma vantagem; gera o código do cupom. */
@Entity
@Table(name = "resgate_vantagem")
@PrimaryKeyJoinColumn(name = "transacao_id")
public class ResgateVantagem extends Transacao {

    @Column(name = "codigo_cupom", nullable = false, unique = true, length = 12)
    private String codigoCupom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusResgate status = StatusResgate.PENDENTE;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_id", nullable = false)
    private Vantagem vantagem;

    public String gerarCodigo() {
        this.codigoCupom = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return codigoCupom;
    }

    /** Chamado pela empresa parceira ao validar o cupom na troca presencial. */
    public void conferir() {
        if (status != StatusResgate.PENDENTE) {
            throw new IllegalStateException("Este cupom já foi " + status.name().toLowerCase() + ".");
        }
        this.status = StatusResgate.CONFERIDO;
    }

    @Override
    public String getDescricao() {
        return "Resgate de " + (vantagem != null ? vantagem.getNome() : "vantagem");
    }

    public String getCodigoCupom() { return codigoCupom; }
    public StatusResgate getStatus() { return status; }
    public void setStatus(StatusResgate status) { this.status = status; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }
    public Vantagem getVantagem() { return vantagem; }
    public void setVantagem(Vantagem vantagem) { this.vantagem = vantagem; }
}
