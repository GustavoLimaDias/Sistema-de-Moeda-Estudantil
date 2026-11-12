package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/** Envio de moedas de um professor para um aluno (motivo obrigatório). */
@Entity
@Table(name = "transferencia_moeda")
@PrimaryKeyJoinColumn(name = "transacao_id")
public class TransferenciaMoeda extends Transacao {

    @Column(nullable = false, length = 500)
    private String motivo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor remetente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno destinatario;

    @Override
    public String getDescricao() {
        return "Envio de " + getValor() + " moedas: " + motivo;
    }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Professor getRemetente() { return remetente; }
    public void setRemetente(Professor remetente) { this.remetente = remetente; }
    public Aluno getDestinatario() { return destinatario; }
    public void setDestinatario(Aluno destinatario) { this.destinatario = destinatario; }
}
