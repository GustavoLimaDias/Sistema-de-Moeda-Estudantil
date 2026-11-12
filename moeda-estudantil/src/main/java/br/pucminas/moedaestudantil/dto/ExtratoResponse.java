package br.pucminas.moedaestudantil.dto;

import java.util.List;

public record ExtratoResponse(int saldo, int totalEntradas, int totalSaidas, List<ItemExtratoResponse> transacoes) {
}
