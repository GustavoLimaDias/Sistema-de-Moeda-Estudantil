package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.TransferenciaMoeda;

import java.time.LocalDateTime;

public record TransferenciaResponse(Long id, LocalDateTime dataHora, int valor, String motivo,
                                    Long alunoId, String alunoNome, int saldoProfessor) {

    public static TransferenciaResponse from(TransferenciaMoeda t) {
        return new TransferenciaResponse(t.getId(), t.getDataHora(), t.getValor(), t.getMotivo(),
                t.getDestinatario().getId(), t.getDestinatario().getNome(), t.getRemetente().getSaldo());
    }
}
