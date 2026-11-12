package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record VantagemRequest(
        @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
        @NotBlank(message = "Informe a descrição") @Size(max = 1000) String descricao,
        @NotBlank(message = "Informe a URL da foto") @Size(max = 500) String fotoUrl,
        @NotNull(message = "Informe o custo") @Positive(message = "O custo deve ser maior que zero") Integer custo,
        Boolean ativa
) {
}
