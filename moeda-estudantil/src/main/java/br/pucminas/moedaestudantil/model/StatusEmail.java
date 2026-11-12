package br.pucminas.moedaestudantil.model;

public enum StatusEmail {
    /** Entregue ao servidor SMTP. */
    ENVIADO,
    /** Sem SMTP configurado: apenas registrado (modo de desenvolvimento). */
    SIMULADO,
    /** O servidor SMTP recusou ou não respondeu. */
    FALHOU
}
