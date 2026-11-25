package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dto.ExtratoResponse;
import br.pucminas.moedaestudantil.dto.ItemExtratoResponse;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.CreditoSemestral;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.model.StatusResgate;
import br.pucminas.moedaestudantil.model.TransferenciaMoeda;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.CreditoSemestralRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.ResgateVantagemRepository;
import br.pucminas.moedaestudantil.repository.TransferenciaMoedaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExtratoService {

    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final TransferenciaMoedaRepository transferenciaRepository;
    private final ResgateVantagemRepository resgateRepository;
    private final CreditoSemestralRepository creditoRepository;

    public ExtratoService(ProfessorRepository professorRepository, AlunoRepository alunoRepository,
                          TransferenciaMoedaRepository transferenciaRepository,
                          ResgateVantagemRepository resgateRepository, CreditoSemestralRepository creditoRepository) {
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.resgateRepository = resgateRepository;
        this.creditoRepository = creditoRepository;
    }

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
