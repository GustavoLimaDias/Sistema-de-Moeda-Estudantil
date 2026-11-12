package br.pucminas.moedaestudantil.security;

import br.pucminas.moedaestudantil.model.Perfil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restringe o endpoint aos perfis informados. Sem esta anotação, basta estar autenticado.
 * A anotação no método tem precedência sobre a da classe.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequerPerfil {
    Perfil[] value();
}
