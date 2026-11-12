package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dto.EnvioMoedasRequest;
import br.pucminas.moedaestudantil.dto.ExtratoResponse;
import br.pucminas.moedaestudantil.dto.ItemExtratoResponse;
import br.pucminas.moedaestudantil.dto.ResgateResponse;
import br.pucminas.moedaestudantil.dto.TransferenciaResponse;
import br.pucminas.moedaestudantil.event.MoedasEnviadasEvent;
import br.pucminas.moedaestudantil.event.ResgateCanceladoEvent;
import br.pucminas.moedaestudantil.event.VantagemResgatadaEvent;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.CreditoSemestral;
import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.model.StatusResgate;
import br.pucminas.moedaestudantil.model.TransferenciaMoeda;
import br.pucminas.moedaestudantil.model.Vantagem;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.CreditoSemestralRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.ResgateVantagemRepository;
import br.pucminas.moedaestudantil.repository.TransferenciaMoedaRepository;
import br.pucminas.moedaestudantil.repository.VantagemRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Movimentações de moeda: envio (UC05), resgate de vantagem (UC08/UC09), conferência (UC16),
 * cancelamento com estorno (UC17) e extratos (UC04).
 * Cada operação roda numa transação com bloqueio pessimista das contas envolvidas,
 * e os emails (UC06/UC10) são disparados por eventos somente após o commit.
 */
@Service
@Transactional
public class TransacaoService {

    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final VantagemRepository vantagemRepository;
    private final TransferenciaMoedaRepository transferenciaRepository;
    private final ResgateVantagemRepository resgateRepository;
    private final CreditoSemestralRepository creditoRepository;
    private final ApplicationEventPublisher eventos;

    public TransacaoService(ProfessorRepository professorRepository, AlunoRepository alunoRepository,
                            VantagemRepository vantagemRepository, TransferenciaMoedaRepository transferenciaRepository,
                            ResgateVantagemRepository resgateRepository, CreditoSemestralRepository creditoRepository,
                            ApplicationEventPublisher eventos) {
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.vantagemRepository = vantagemRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.resgateRepository = resgateRepository;
        this.creditoRepository = creditoRepository;
        this.eventos = eventos;
    }

    // ------------------------------------------------------------------ envio de moedas

    public TransferenciaResponse enviarMoedas(Long professorId, EnvioMoedasRequest request) {
        Professor professor = professorRepository.findParaAtualizarSaldo(professorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado."));
        Aluno aluno = alunoRepository.findParaAtualizarSaldo(request.alunoId())
                .orElseThrow(() -> new RegraNegocioException("alunoId", "Aluno não encontrado."));

        if (!aluno.getInstituicao().getId().equals(professor.getInstituicao().getId())) {
            throw new RegraNegocioException("alunoId", "Este aluno não pertence à sua instituição.");
        }
        int valor = request.valor();
        if (professor.getSaldo() < valor) {
            throw new RegraNegocioException("valor",
                    "Saldo insuficiente: você possui " + professor.getSaldo() + " moedas.");
        }

        professor.debitarMoedas(valor);
        aluno.receberMoedas(valor);

        TransferenciaMoeda t = new TransferenciaMoeda();
        t.setRemetente(professor);
        t.setDestinatario(aluno);
        t.setValor(valor);
        t.setMotivo(request.motivo().trim());
        transferenciaRepository.save(t);

        eventos.publishEvent(new MoedasEnviadasEvent(t.getId()));
        return TransferenciaResponse.from(t);
    }

    // ------------------------------------------------------------------ resgate de vantagem

    public ResgateResponse resgatarVantagem(Long alunoId, Long vantagemId) {
        Aluno aluno = alunoRepository.findParaAtualizarSaldo(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));
        Vantagem vantagem = vantagemRepository.findWithEmpresaById(vantagemId)
                .orElseThrow(() -> new RegraNegocioException("vantagemId", "Vantagem não encontrada."));

        if (!vantagem.isAtiva()) {
            throw new RegraNegocioException("vantagemId", "Esta vantagem não está mais disponível.");
        }
        if (aluno.getSaldo() < vantagem.getCusto()) {
            throw new RegraNegocioException("vantagemId", "Saldo insuficiente: a vantagem custa "
                    + vantagem.getCusto() + " moedas e você possui " + aluno.getSaldo() + ".");
        }

        aluno.debitarMoedas(vantagem.getCusto());

        ResgateVantagem resgate = new ResgateVantagem();
        resgate.setAluno(aluno);
        resgate.setVantagem(vantagem);
        resgate.setValor(vantagem.getCusto());
        do {
            resgate.gerarCodigo();
        } while (resgateRepository.existsByCodigoCupom(resgate.getCodigoCupom()));
        resgateRepository.save(resgate);

        eventos.publishEvent(new VantagemResgatadaEvent(resgate.getId()));
        return ResgateResponse.from(resgate);
    }

    // ------------------------------------------------------------------ conferência pela empresa

    @Transactional(readOnly = true)
    public List<ResgateResponse> resgatesDaEmpresa(Long empresaId) {
        return resgateRepository.findByVantagemEmpresaIdOrderByDataHoraDesc(empresaId).stream()
                .map(ResgateResponse::from).toList();
    }

    /** UC16 – a empresa confirma a troca presencial do cupom. */
    public ResgateResponse conferirCupom(Long empresaId, String codigo) {
        ResgateVantagem resgate = buscarParaAtualizar(codigo);
        exigirDaEmpresa(resgate, empresaId);
        try {
            resgate.conferir();
        } catch (IllegalStateException e) {
            throw new RegraNegocioException("codigo", e.getMessage());
        }
        return ResgateResponse.from(resgate);
    }

    // ------------------------------------------------------------------ cancelamento (UC17)

    /** O aluno desiste de um cupom ainda não usado e recebe as moedas de volta. */
    public ResgateResponse cancelarResgateDoAluno(Long alunoId, String codigo) {
        ResgateVantagem resgate = buscarParaAtualizar(codigo);
        if (!resgate.getAluno().getId().equals(alunoId)) {
            throw new RecursoNaoEncontradoException("Cupom não encontrado.");
        }
        return cancelar(resgate, Perfil.ALUNO);
    }

    /** A empresa cancela um cupom que não pode atender (ex.: produto indisponível); o aluno é reembolsado. */
    public ResgateResponse cancelarResgateDaEmpresa(Long empresaId, String codigo) {
        ResgateVantagem resgate = buscarParaAtualizar(codigo);
        exigirDaEmpresa(resgate, empresaId);
        return cancelar(resgate, Perfil.EMPRESA);
    }

    private ResgateResponse cancelar(ResgateVantagem resgate, Perfil canceladoPor) {
        Aluno aluno = alunoRepository.findParaAtualizarSaldo(resgate.getAluno().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));
        try {
            resgate.cancelar();
        } catch (IllegalStateException e) {
            throw new RegraNegocioException("codigo", e.getMessage());
        }
        aluno.receberMoedas(resgate.getValor());
        eventos.publishEvent(new ResgateCanceladoEvent(resgate.getId(), canceladoPor));
        return ResgateResponse.from(resgate);
    }

    private ResgateVantagem buscarParaAtualizar(String codigo) {
        return resgateRepository.findParaAtualizarStatus(codigo.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cupom não encontrado."));
    }

    private void exigirDaEmpresa(ResgateVantagem resgate, Long empresaId) {
        if (!resgate.getVantagem().getEmpresa().getId().equals(empresaId)) {
            throw new RecursoNaoEncontradoException("Cupom não encontrado para a sua empresa.");
        }
    }

    // ------------------------------------------------------------------ extratos

    @Transactional(readOnly = true)
    public ExtratoResponse extratoAluno(Long alunoId) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));
        List<ItemExtratoResponse> itens = new ArrayList<>();
        for (TransferenciaMoeda t : transferenciaRepository.findByDestinatarioIdOrderByDataHoraDesc(alunoId)) {
            itens.add(new ItemExtratoResponse(t.getId(), t.getDataHora(), "RECEBIMENTO", t.getValor(),
                    t.getMotivo(), t.getRemetente().getNome(), null, null));
        }
        for (ResgateVantagem r : resgateRepository.findByAlunoIdOrderByDataHoraDesc(alunoId)) {
            itens.add(new ItemExtratoResponse(r.getId(), r.getDataHora(), "RESGATE", -r.getValor(),
                    r.getVantagem().getNome(), r.getVantagem().getEmpresa().getNome(), r.getCodigoCupom(),
                    r.getStatus().name()));
            if (r.getStatus() == StatusResgate.CANCELADO) {
                itens.add(new ItemExtratoResponse(r.getId(), r.getDataCancelamento(), "ESTORNO", r.getValor(),
                        "Estorno: " + r.getVantagem().getNome(), r.getVantagem().getEmpresa().getNome(),
                        r.getCodigoCupom(), r.getStatus().name()));
            }
        }
        return montar(aluno.getSaldo(), itens);
    }

    @Transactional(readOnly = true)
    public ExtratoResponse extratoProfessor(Long professorId) {
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado."));
        List<ItemExtratoResponse> itens = new ArrayList<>();
        for (TransferenciaMoeda t : transferenciaRepository.findByRemetenteIdOrderByDataHoraDesc(professorId)) {
            itens.add(new ItemExtratoResponse(t.getId(), t.getDataHora(), "ENVIO", -t.getValor(),
                    t.getMotivo(), t.getDestinatario().getNome(), null, null));
        }
        for (CreditoSemestral c : creditoRepository.findByProfessorIdOrderByDataHoraDesc(professorId)) {
            itens.add(new ItemExtratoResponse(c.getId(), c.getDataHora(), "CREDITO", c.getValor(),
                    "Crédito do semestre " + c.getSemestre(), "Sistema", null, null));
        }
        return montar(professor.getSaldo(), itens);
    }

    private ExtratoResponse montar(int saldo, List<ItemExtratoResponse> itens) {
        itens.sort(Comparator.comparing(ItemExtratoResponse::dataHora).reversed()
                .thenComparing(ItemExtratoResponse::id, Comparator.reverseOrder()));
        int entradas = itens.stream().filter(i -> i.valor() > 0).mapToInt(ItemExtratoResponse::valor).sum();
        int saidas = itens.stream().filter(i -> i.valor() < 0).mapToInt(i -> -i.valor()).sum();
        return new ExtratoResponse(saldo, entradas, saidas, itens);
    }
}
