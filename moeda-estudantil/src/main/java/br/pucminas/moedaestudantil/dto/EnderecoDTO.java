package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoDTO(
        @NotBlank(message = "Informe o logradouro") @Size(max = 150) String logradouro,
        @NotBlank(message = "Informe o número") @Size(max = 10) String numero,
        @Size(max = 60) String complemento,
        @NotBlank(message = "Informe o bairro") @Size(max = 80) String bairro,
        @NotBlank(message = "Informe a cidade") @Size(max = 80) String cidade,
        @NotBlank(message = "Informe a UF") @Pattern(regexp = "[A-Za-z]{2}", message = "UF deve ter 2 letras") String uf,
        @NotBlank(message = "Informe o CEP") @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido") String cep
) {
    public Endereco toEntity() {
        return new Endereco(logradouro.trim(), numero.trim(),
                complemento == null || complemento.isBlank() ? null : complemento.trim(),
                bairro.trim(), cidade.trim(), uf.toUpperCase(), cep.replace("-", ""));
    }

    public static EnderecoDTO from(Endereco e) {
        if (e == null) {
            return null;
        }
        return new EnderecoDTO(e.getLogradouro(), e.getNumero(), e.getComplemento(), e.getBairro(),
                e.getCidade(), e.getUf(), e.getCep());
    }
}
