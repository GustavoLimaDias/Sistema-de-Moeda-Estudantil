package br.pucminas.moedaestudantil.dto;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato padrão de erro devolvido pela API ao front-end. */
public record ErroResponse(int status, String mensagem, Map<String, String> campos, LocalDateTime timestamp) {

    public static ErroResponse of(int status, String mensagem) {
        return new ErroResponse(status, mensagem, Map.of(), LocalDateTime.now());
    }

    public static ErroResponse of(int status, String mensagem, Map<String, String> campos) {
        return new ErroResponse(status, mensagem, campos, LocalDateTime.now());
    }
}
