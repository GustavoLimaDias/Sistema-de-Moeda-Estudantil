package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Registro de cada email disparado pelo sistema (auditoria e caixa de entrada de demonstração). */
@Entity
@Table(name = "notificacao_email")
public class NotificacaoEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String destinatario;

    @Column(nullable = false, length = 200)
    private String assunto;

    @Column(nullable = false, length = 4000)
    private String corpo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEmail status;

    public NotificacaoEmail() {
    }

    public NotificacaoEmail(String destinatario, String assunto, String corpo) {
        this.destinatario = destinatario;
        this.assunto = assunto;
        this.corpo = corpo;
        this.dataHora = LocalDateTime.now();
        this.status = StatusEmail.SIMULADO;
    }

    public Long getId() { return id; }
    public String getDestinatario() { return destinatario; }
    public String getAssunto() { return assunto; }
    public String getCorpo() { return corpo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public StatusEmail getStatus() { return status; }
    public void setStatus(StatusEmail status) { this.status = status; }
}
