package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dto.NotificacaoResponse;
import br.pucminas.moedaestudantil.event.MoedasEnviadasEvent;
import br.pucminas.moedaestudantil.event.VantagemResgatadaEvent;
import br.pucminas.moedaestudantil.model.NotificacaoEmail;
import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.model.StatusEmail;
import br.pucminas.moedaestudantil.model.TransferenciaMoeda;
import br.pucminas.moedaestudantil.repository.NotificacaoEmailRepository;
import br.pucminas.moedaestudantil.repository.ResgateVantagemRepository;
import br.pucminas.moedaestudantil.repository.TransferenciaMoedaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * UC06 e UC10 – emails do sistema. Ouve os eventos de domínio somente depois do commit
 * (um email nunca sai para uma transação desfeita). Todo email é registrado na tabela
 * notificacao_email; se houver SMTP configurado (spring.mail.host) ele também é enviado.
 */
@Service
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificacaoEmailRepository notificacaoRepository;
    private final TransferenciaMoedaRepository transferenciaRepository;
    private final ResgateVantagemRepository resgateRepository;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remetente;

    public NotificacaoService(NotificacaoEmailRepository notificacaoRepository,
                              TransferenciaMoedaRepository transferenciaRepository,
                              ResgateVantagemRepository resgateRepository,
                              ObjectProvider<JavaMailSender> mailSender,
                              @Value("${app.email.remetente:Moeda Estudantil <nao-responda@moedaestudantil.com>}") String remetente) {
        this.notificacaoRepository = notificacaoRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.resgateRepository = resgateRepository;
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoEnviarMoedas(MoedasEnviadasEvent evento) {
        TransferenciaMoeda t = transferenciaRepository.findWithParticipantesById(evento.transferenciaId()).orElseThrow();
        String corpo = """
                Olá, %s!

                Você recebeu %d moedas do(a) professor(a) %s.

                Motivo: %s

                Seu saldo atual é de %d moedas. Acesse o sistema para ver o extrato e as vantagens disponíveis.

                Sistema de Moeda Estudantil
                """.formatted(t.getDestinatario().getNome(), t.getValor(), t.getRemetente().getNome(),
                t.getMotivo(), t.getDestinatario().getSaldo());
        enviar(t.getDestinatario().getEmail(), "Você recebeu " + t.getValor() + " moedas!", corpo);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoResgatarVantagem(VantagemResgatadaEvent evento) {
        ResgateVantagem r = resgateRepository.findWithDetalhesById(evento.resgateId()).orElseThrow();
        String quando = r.getDataHora().format(DATA);

        String cupomAluno = """
                Olá, %s!

                Seu resgate foi confirmado.

                Vantagem: %s
                Empresa parceira: %s
                Custo: %d moedas
                Data: %s

                CÓDIGO DO CUPOM: %s

                Apresente este código na empresa parceira para realizar a troca.

                Sistema de Moeda Estudantil
                """.formatted(r.getAluno().getNome(), r.getVantagem().getNome(), r.getVantagem().getEmpresa().getNome(),
                r.getValor(), quando, r.getCodigoCupom());
        enviar(r.getAluno().getEmail(), "Seu cupom: " + r.getVantagem().getNome(), cupomAluno);

        String avisoEmpresa = """
                Olá, %s!

                Um aluno resgatou uma de suas vantagens.

                Aluno: %s (%s)
                Vantagem: %s
                Data: %s

                CÓDIGO PARA CONFERÊNCIA: %s

                Na troca presencial, confira se o código apresentado pelo aluno é o mesmo
                e registre a conferência no sistema.

                Sistema de Moeda Estudantil
                """.formatted(r.getVantagem().getEmpresa().getNome(), r.getAluno().getNome(), r.getAluno().getEmail(),
                r.getVantagem().getNome(), quando, r.getCodigoCupom());
        enviar(r.getVantagem().getEmpresa().getEmail(), "Novo resgate: " + r.getVantagem().getNome(), avisoEmpresa);
    }

    /** Caixa de entrada de demonstração: emails enviados para o usuário logado. */
    @Transactional(readOnly = true)
    public List<NotificacaoResponse> doDestinatario(String email) {
        return notificacaoRepository.findByDestinatarioIgnoreCaseOrderByDataHoraDesc(email).stream()
                .map(NotificacaoResponse::from).toList();
    }

    /** Últimos 100 emails do sistema (painel do administrador). */
    @Transactional(readOnly = true)
    public List<NotificacaoResponse> recentes() {
        return notificacaoRepository.findTop100ByOrderByDataHoraDesc().stream()
                .map(NotificacaoResponse::from).toList();
    }

    private void enviar(String destinatario, String assunto, String corpo) {
        NotificacaoEmail email = new NotificacaoEmail(destinatario, assunto, corpo);
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender != null) {
            try {
                SimpleMailMessage mensagem = new SimpleMailMessage();
                mensagem.setFrom(remetente);
                mensagem.setTo(destinatario);
                mensagem.setSubject(assunto);
                mensagem.setText(corpo);
                sender.send(mensagem);
                email.setStatus(StatusEmail.ENVIADO);
            } catch (RuntimeException e) {
                log.warn("Falha ao enviar email para {}: {}", destinatario, e.getMessage());
                email.setStatus(StatusEmail.FALHOU);
            }
        } else {
            log.info("[email simulado] para={} assunto=\"{}\"", destinatario, assunto);
        }
        notificacaoRepository.save(email);
    }
}
