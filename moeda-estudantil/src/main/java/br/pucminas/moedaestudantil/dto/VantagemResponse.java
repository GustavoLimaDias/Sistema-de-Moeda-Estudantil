package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Vantagem;

public record VantagemResponse(
        Long id,
        String nome,
        String descricao,
        String fotoUrl,
        int custo,
        boolean ativa,
        Long empresaId
) {
    public static VantagemResponse from(Vantagem v) {
        return new VantagemResponse(v.getId(), v.getNome(), v.getDescricao(), v.getFotoUrl(), v.getCusto(),
                v.isAtiva(), v.getEmpresa().getId());
    }
}
