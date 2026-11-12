package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.ResgateVantagem;

import java.time.LocalDateTime;

/** Resgate do ponto de vista do aluno (cupom) e da empresa (conferência). */
public record ResgateResponse(
        Long id,
        String codigoCupom,
        String status,
        LocalDateTime dataHora,
        int custo,
        Long vantagemId,
        String vantagemNome,
        String empresaNome,
        Long alunoId,
        String alunoNome,
        String alunoEmail,
        int saldoAluno,
        LocalDateTime dataCancelamento
) {
    public static ResgateResponse from(ResgateVantagem r) {
        return new ResgateResponse(r.getId(), r.getCodigoCupom(), r.getStatus().name(), r.getDataHora(),
                r.getValor(), r.getVantagem().getId(), r.getVantagem().getNome(),
                r.getVantagem().getEmpresa().getNome(), r.getAluno().getId(), r.getAluno().getNome(),
                r.getAluno().getEmail(), r.getAluno().getSaldo(), r.getDataCancelamento());
    }
}
