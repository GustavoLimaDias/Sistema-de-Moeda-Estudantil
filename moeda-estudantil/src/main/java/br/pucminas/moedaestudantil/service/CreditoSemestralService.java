package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dto.CreditoSemestralResponse;
import br.pucminas.moedaestudantil.model.CreditoSemestral;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.repository.CreditoSemestralRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * UC12 – Creditar 1.000 moedas por semestre a cada professor (valor acumulativo).
 * Disparado pelo agendador no início de cada semestre (1º de janeiro e 1º de julho)
 * e também na inicialização, para não perder um semestre se o servidor estava desligado.
 * É idempotente: cada professor recebe no máximo um crédito por semestre.
 */
@Service
public class CreditoSemestralService {

    private static final Logger log = LoggerFactory.getLogger(CreditoSemestralService.class);

    private final ProfessorRepository professorRepository;
    private final CreditoSemestralRepository creditoRepository;

    public CreditoSemestralService(ProfessorRepository professorRepository,
                                   CreditoSemestralRepository creditoRepository) {
        this.professorRepository = professorRepository;
        this.creditoRepository = creditoRepository;
    }

    public static String semestreDe(LocalDate data) {
        return data.getYear() + "/" + (data.getMonthValue() <= 6 ? 1 : 2);
    }

    @Transactional
    public CreditoSemestralResponse creditar(String semestre) {
        int creditados = 0;
        for (Professor professor : professorRepository.findAll()) {
            if (creditarSeNecessario(professor.getId(), semestre)) {
                creditados++;
            }
        }
        if (creditados > 0) {
            log.info("Crédito semestral {}: {} professor(es) receberam {} moedas.", semestre, creditados,
                    Professor.CREDITO_SEMESTRAL);
        }
        return new CreditoSemestralResponse(semestre, creditados, Professor.CREDITO_SEMESTRAL);
    }

    /** Credita o professor no semestre, se ainda não tiver recebido. Retorna true se creditou. */
    @Transactional
    public boolean creditarSeNecessario(Long professorId, String semestre) {
        if (creditoRepository.existsByProfessorIdAndSemestre(professorId, semestre)) {
            return false;
        }
        Professor professor = professorRepository.findParaAtualizarSaldo(professorId).orElseThrow();
        professor.creditarSemestre(Professor.CREDITO_SEMESTRAL);
        CreditoSemestral credito = new CreditoSemestral();
        credito.setProfessor(professor);
        credito.setSemestre(semestre);
        credito.setValor(Professor.CREDITO_SEMESTRAL);
        creditoRepository.save(credito);
        return true;
    }

    @Transactional
    public CreditoSemestralResponse creditarSemestreAtual() {
        return creditar(semestreDe(LocalDate.now()));
    }

    /** Início de cada semestre: 00:05 de 1º de janeiro e de 1º de julho. */
    @Scheduled(cron = "${app.credito.cron:0 5 0 1 1,7 *}")
    @Transactional
    public void executarAgendado() {
        creditarSemestreAtual();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void executarNaInicializacao() {
        creditarSemestreAtual();
    }
}
