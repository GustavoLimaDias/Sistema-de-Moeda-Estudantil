package br.pucminas.moedaestudantil.security;

import br.pucminas.moedaestudantil.exception.AcessoNegadoException;
import br.pucminas.moedaestudantil.exception.NaoAutenticadoException;
import br.pucminas.moedaestudantil.model.Perfil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * Executado antes de todo endpoint /api/**: exige token válido (exceto em @Publico)
 * e confere o perfil quando houver @RequerPerfil.
 */
@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_SESSAO = "sessaoUsuario";

    private final TokenService tokenService;

    public AutenticacaoInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod metodo)) {
            return true; // recursos estáticos e requisições de pré-voo CORS
        }

        String token = extrairToken(request);
        SessaoUsuario sessao = tokenService.validar(token).orElse(null);
        if (sessao != null) {
            request.setAttribute(ATRIBUTO_SESSAO, sessao);
        }

        boolean publico = metodo.hasMethodAnnotation(Publico.class)
                || metodo.getBeanType().isAnnotationPresent(Publico.class);
        if (publico) {
            return true;
        }
        if (sessao == null) {
            throw new NaoAutenticadoException("Faça login para continuar.");
        }

        RequerPerfil requer = metodo.getMethodAnnotation(RequerPerfil.class);
        if (requer == null) {
            requer = metodo.getBeanType().getAnnotation(RequerPerfil.class);
        }
        if (requer != null) {
            Perfil[] permitidos = requer.value();
            if (Arrays.stream(permitidos).noneMatch(p -> p == sessao.perfil())) {
                throw new AcessoNegadoException("Seu perfil não tem acesso a esta operação.");
            }
        }
        return true;
    }

    public static String extrairToken(HttpServletRequest request) {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            return cabecalho.substring(7).trim();
        }
        return null;
    }
}
