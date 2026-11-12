package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.CreditoSemestralResponse;
import br.pucminas.moedaestudantil.dto.NotificacaoResponse;
import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.security.RequerPerfil;
import br.pucminas.moedaestudantil.service.CreditoSemestralService;
import br.pucminas.moedaestudantil.service.NotificacaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequerPerfil(Perfil.ADMIN)
public class AdminController {

    private final CreditoSemestralService creditoSemestralService;
    private final NotificacaoService notificacaoService;

    public AdminController(CreditoSemestralService creditoSemestralService, NotificacaoService notificacaoService) {
        this.creditoSemestralService = creditoSemestralService;
        this.notificacaoService = notificacaoService;
    }

    /** Executa manualmente o crédito do semestre atual (o agendador faz isso automaticamente). */
    @PostMapping("/creditos-semestrais")
    public CreditoSemestralResponse creditarSemestre() {
        return creditoSemestralService.creditarSemestreAtual();
    }

    /** Últimos emails disparados pelo sistema. */
    @GetMapping("/emails")
    public List<NotificacaoResponse> emails() {
        return notificacaoService.recentes();
    }
}
