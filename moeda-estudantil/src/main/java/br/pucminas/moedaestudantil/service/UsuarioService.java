package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Usuario;
import br.pucminas.moedaestudantil.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Regras comuns a todos os perfis: unicidade de email/login e senha.
 * Reaproveitado pelos serviços de aluno e empresa (e, na Sprint 3, pela autenticação).
 */
@Service
public class UsuarioService {

    public static final int TAMANHO_MINIMO_SENHA = 6;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Valida e aplica nome, email, login e senha. {@code id} é nulo no cadastro. */
    public void aplicarDadosDeAcesso(Usuario usuario, Long id, String nome, String email, String login, String senha) {
        String emailNormalizado = email.trim().toLowerCase();
        String loginNormalizado = login.trim();

        boolean emailEmUso = id == null
                ? usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)
                : usuarioRepository.existsByEmailIgnoreCaseAndIdNot(emailNormalizado, id);
        if (emailEmUso) {
            throw new RegraNegocioException("email", "Este email já está em uso.");
        }

        boolean loginEmUso = id == null
                ? usuarioRepository.existsByLogin(loginNormalizado)
                : usuarioRepository.existsByLoginAndIdNot(loginNormalizado, id);
        if (loginEmUso) {
            throw new RegraNegocioException("login", "Este login já está em uso.");
        }

        boolean senhaInformada = senha != null && !senha.isBlank();
        if (id == null && !senhaInformada) {
            throw new RegraNegocioException("senha", "Informe a senha.");
        }
        if (senhaInformada) {
            if (senha.length() < TAMANHO_MINIMO_SENHA) {
                throw new RegraNegocioException("senha",
                        "A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
            }
            usuario.alterarSenha(senha, passwordEncoder);
        }

        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setLogin(loginNormalizado);
    }
}
