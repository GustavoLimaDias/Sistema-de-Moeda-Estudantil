package br.pucminas.moedaestudantil.config;

import br.pucminas.moedaestudantil.security.AutenticacaoInterceptor;
import br.pucminas.moedaestudantil.security.UsuarioLogadoResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AutenticacaoInterceptor autenticacaoInterceptor;
    private final UsuarioLogadoResolver usuarioLogadoResolver;
    private final String[] origensExtras;

    public WebConfig(AutenticacaoInterceptor autenticacaoInterceptor, UsuarioLogadoResolver usuarioLogadoResolver,
                     @Value("${app.cors.allowed-origins:}") String[] origensExtras) {
        this.autenticacaoInterceptor = autenticacaoInterceptor;
        this.usuarioLogadoResolver = usuarioLogadoResolver;
        this.origensExtras = origensExtras;
    }

    /** Autenticação e autorização de toda a API. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autenticacaoInterceptor).addPathPatterns("/api/**");
    }

    /** Permite parâmetros "@UsuarioLogado SessaoUsuario sessao" nos controllers. */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(usuarioLogadoResolver);
    }

    /**
     * Permite rodar o front-end em outro servidor local (ex.: Live Server) e, em produção,
     * as origens de app.cors.allowed-origins (ex.: https://*.vercel.app).
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origens = Stream.concat(Stream.of("http://localhost:*", "http://127.0.0.1:*"),
                        Arrays.stream(origensExtras).map(String::trim).filter(o -> !o.isEmpty()))
                .toArray(String[]::new);
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origens)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
