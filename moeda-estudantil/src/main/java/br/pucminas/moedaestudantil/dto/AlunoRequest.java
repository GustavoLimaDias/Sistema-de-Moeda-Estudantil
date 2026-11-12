package br.pucminas.moedaestudantil.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada do cadastro/edição de aluno.
 * A senha é obrigatória no cadastro e opcional na edição (vazia = mantém a atual).
 */
public record AlunoRequest(
        @NotBlank(message = "Informe o nome") @Size(max = 120) String nome,
        @NotBlank(message = "Informe o email") @Email(message = "Email inválido") @Size(max = 120) String email,
        @NotBlank(message = "Informe o CPF") String cpf,
        @NotBlank(message = "Informe o RG") @Size(max = 20) String rg,
        @NotBlank(message = "Informe o curso") @Size(max = 100) String curso,
        @NotNull(message = "Selecione a instituição") Long instituicaoId,
        @NotNull(message = "Informe o endereço") @Valid EnderecoDTO endereco,
        @NotBlank(message = "Informe o login") @Size(min = 3, max = 60, message = "Login deve ter de 3 a 60 caracteres") String login,
        String senha
) {
}
