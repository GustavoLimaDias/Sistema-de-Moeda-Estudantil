package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.InstituicaoEnsino;

public record InstituicaoResponse(Long id, String nome, String cnpj) {

    public static InstituicaoResponse from(InstituicaoEnsino i) {
        return new InstituicaoResponse(i.getId(), i.getNome(), i.getCnpj());
    }
}
