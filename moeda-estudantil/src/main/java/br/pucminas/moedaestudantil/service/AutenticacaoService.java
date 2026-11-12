package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dto.LoginRequest;
import br.pucminas.moedaestudantil.dto.LoginResponse;
import br.pucminas.moedaestudantil.exception.NaoAutenticadoException;
import br.pucminas.moedaestudantil.model.Usuario;
import br.pucminas.moedaestudantil.repository.UsuarioRepository;
import br.pucminas.moedaestudantil.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** UC01 – Realizar login. Vale para todos os perfis (aluno, professor, empresa e administrador). */
@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AutenticacaoService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                               TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String login = request.login().trim();
        Usuario usuario = usuarioRepository.findByLogin(login)
                .filter(u -> u.autenticar(login, request.senha(), passwordEncoder))
                .orElseThrow(() -> new NaoAutenticadoException("Login ou senha inválidos."));
        String token = tokenService.emitir(usuario);
        return new LoginResponse(token, tokenService.validar(token).orElseThrow());
    }

    public void logout(String token) {
        tokenService.revogar(token);
    }
}
