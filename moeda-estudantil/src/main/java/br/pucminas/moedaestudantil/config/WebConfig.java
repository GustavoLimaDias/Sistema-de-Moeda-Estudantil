package br.pucminas.moedaestudantil.config;

import br.pucminas.moedaestudantil.security.AutenticacaoInterceptor;
import br.pucminas.moedaestudantil.security.UsuarioLogadoResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AutenticacaoInterceptor autenticacaoInterceptor;
    private final UsuarioLogadoResolver usuarioLogadoResolver;

    public WebConfig(AutenticacaoInterceptor autenticacaoInterceptor, UsuarioLogadoResolver usuarioLogadoResolver) {
        this.autenticacaoInterceptor = autenticacaoInterceptor;
        this.usuarioLogadoResolver = usuarioLogadoResolver;
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

    /** Permite rodar o front-end em outro servidor local (ex.: Live Server) durante o desenvolvimento. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
