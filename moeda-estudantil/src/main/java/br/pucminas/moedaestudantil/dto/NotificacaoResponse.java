package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.NotificacaoEmail;

import java.time.LocalDateTime;

public record NotificacaoResponse(Long id, String destinatario, String assunto, String corpo,
                                  LocalDateTime dataHora, String status) {

    public static NotificacaoResponse from(NotificacaoEmail n) {
        return new NotificacaoResponse(n.getId(), n.getDestinatario(), n.getAssunto(), n.getCorpo(),
                n.getDataHora(), n.getStatus().name());
    }
}
