package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.LoginRequest;
import br.pucminas.moedaestudantil.dto.LoginResponse;
import br.pucminas.moedaestudantil.dto.NotificacaoResponse;
import br.pucminas.moedaestudantil.security.AutenticacaoInterceptor;
import br.pucminas.moedaestudantil.security.Publico;
import br.pucminas.moedaestudantil.security.SessaoUsuario;
import br.pucminas.moedaestudantil.security.UsuarioLogado;
import br.pucminas.moedaestudantil.service.AutenticacaoService;
import br.pucminas.moedaestudantil.service.NotificacaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** UC01 – login/logout para todos os perfis. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticacaoService autenticacaoService;
    private final NotificacaoService notificacaoService;

    public AuthController(AutenticacaoService autenticacaoService, NotificacaoService notificacaoService) {
        this.autenticacaoService = autenticacaoService;
        this.notificacaoService = notificacaoService;
    }

    @Publico
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return autenticacaoService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        autenticacaoService.logout(AutenticacaoInterceptor.extrairToken(request));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public SessaoUsuario me(@UsuarioLogado SessaoUsuario sessao) {
        return sessao;
    }

    /** Emails que o sistema enviou para o usuário logado (caixa de entrada de demonstração). */
    @GetMapping("/emails")
    public List<NotificacaoResponse> meusEmails(@UsuarioLogado SessaoUsuario sessao) {
        return notificacaoService.doDestinatario(sessao.email());
    }
}
