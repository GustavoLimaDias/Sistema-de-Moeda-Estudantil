package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** UC15 – troca de senha feita pelo próprio usuário (qualquer perfil). */
public record AlterarSenhaRequest(
        @NotBlank(message = "Informe a senha atual") String senhaAtual,
        @NotBlank(message = "Informe a nova senha")
        @Size(min = 6, max = 72, message = "A nova senha deve ter de 6 a 72 caracteres") String novaSenha
) {
}
