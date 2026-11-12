package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Vantagem;

/** Vantagem como aparece no catálogo do aluno, com o nome da empresa. */
public record CatalogoVantagemResponse(Long id, String nome, String descricao, String fotoUrl, int custo,
                                       Long empresaId, String empresaNome) {

    public static CatalogoVantagemResponse from(Vantagem v) {
        return new CatalogoVantagemResponse(v.getId(), v.getNome(), v.getDescricao(), v.getFotoUrl(), v.getCusto(),
                v.getEmpresa().getId(), v.getEmpresa().getNome());
    }
}
