package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Pré-cadastro de professor (feito pelo administrador a partir da lista da instituição). */
public record ProfessorRequest(
        @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
        @NotBlank(message = "Informe o email") @Email(message = "Email inválido") @Size(max = 120) String email,
        @NotBlank(message = "Informe o CPF") String cpf,
        @NotBlank(message = "Informe o departamento") @Size(max = 100) String departamento,
        @NotNull(message = "Selecione a instituição") Long instituicaoId,
        @NotBlank(message = "Informe o login") @Size(min = 3, max = 60, message = "Login deve ter de 3 a 60 caracteres") String login,
        String senha
) {
}
