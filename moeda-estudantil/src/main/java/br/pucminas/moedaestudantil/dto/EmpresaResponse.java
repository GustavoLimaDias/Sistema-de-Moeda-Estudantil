package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.EmpresaParceira;

public record EmpresaResponse(
        Long id,
        String nome,
        String email,
        String login,
        String cnpj,
        String descricao,
        int quantidadeVantagens
) {
    public static EmpresaResponse from(EmpresaParceira e) {
        return new EmpresaResponse(e.getId(), e.getNome(), e.getEmail(), e.getLogin(), e.getCnpj(),
                e.getDescricao(), e.getVantagens().size());
    }
}
