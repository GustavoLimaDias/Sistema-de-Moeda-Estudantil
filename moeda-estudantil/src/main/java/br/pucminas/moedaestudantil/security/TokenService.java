package br.pucminas.moedaestudantil.security;

import br.pucminas.moedaestudantil.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Emite e valida tokens de sessão opacos (aleatórios, 256 bits) guardados em memória.
 * O front-end envia o token no cabeçalho "Authorization: Bearer ...".
 */
@Service
public class TokenService {

    private record Entrada(SessaoUsuario sessao, Instant expiraEm) {
    }

    private final Map<String, Entrada> tokens = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Duration validade;

    public TokenService(@Value("${app.sessao.validade-horas:8}") long validadeHoras) {
        this.validade = Duration.ofHours(validadeHoras);
    }

    public String emitir(Usuario usuario) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        SessaoUsuario sessao = new SessaoUsuario(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil());
        tokens.put(token, new Entrada(sessao, Instant.now().plus(validade)));
        return token;
    }

    public Optional<SessaoUsuario> validar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Entrada entrada = tokens.get(token);
        if (entrada == null) {
            return Optional.empty();
        }
        if (entrada.expiraEm().isBefore(Instant.now())) {
            tokens.remove(token);
            return Optional.empty();
        }
        return Optional.of(entrada.sessao());
    }

    public void revogar(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }

    /** Encerra todas as sessões de um usuário (ex.: conta excluída). */
    public void revogarDoUsuario(Long usuarioId) {
        tokens.values().removeIf(e -> e.sessao().id().equals(usuarioId));
    }

    /** Mantém nome/email atualizados nas sessões abertas após edição do perfil. */
    public void atualizarDados(Usuario usuario) {
        tokens.replaceAll((t, e) -> e.sessao().id().equals(usuario.getId())
                ? new Entrada(new SessaoUsuario(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil()), e.expiraEm())
                : e);
    }
}
