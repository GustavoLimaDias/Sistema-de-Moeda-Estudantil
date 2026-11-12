package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InstituicaoRequest(
        @NotBlank(message = "Informe o nome") @Size(max = 150) String nome,
        @NotBlank(message = "Informe o CNPJ") String cnpj
) {
}
