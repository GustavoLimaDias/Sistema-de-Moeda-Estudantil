package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(
        @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
        @NotBlank(message = "Informe o email") @Email(message = "Email inválido") @Size(max = 120) String email,
        @NotBlank(message = "Informe o CNPJ") String cnpj,
        @Size(max = 500) String descricao,
        @NotBlank(message = "Informe o login") @Size(min = 3, max = 60, message = "Login deve ter de 3 a 60 caracteres") String login,
        String senha
) {
}
