package br.pucminas.moedaestudantil.dto;

import java.time.LocalDateTime;

/**
 * Linha do extrato. {@code valor} é positivo para entradas (recebimento, crédito semestral)
 * e negativo para saídas (envio, resgate).
 */
public record ItemExtratoResponse(
        Long id,
        LocalDateTime dataHora,
        String tipo,
        int valor,
        String descricao,
        String contraparte,
        String codigoCupom,
        String status
) {
}
