package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.NotNull;

public record ResgateRequest(@NotNull(message = "Selecione a vantagem") Long vantagemId) {
}
