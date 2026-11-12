package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Aluno;

/** Saída da API: nunca expõe o hash da senha. */
public record AlunoResponse(
        Long id,
        String nome,
        String email,
        String login,
        String cpf,
        String rg,
        String curso,
        int saldo,
        InstituicaoResponse instituicao,
        EnderecoDTO endereco
) {
    public static AlunoResponse from(Aluno a) {
        return new AlunoResponse(a.getId(), a.getNome(), a.getEmail(), a.getLogin(), a.getCpf(), a.getRg(),
                a.getCurso(), a.getSaldo(), InstituicaoResponse.from(a.getInstituicao()),
                EnderecoDTO.from(a.getEndereco()));
    }
}
