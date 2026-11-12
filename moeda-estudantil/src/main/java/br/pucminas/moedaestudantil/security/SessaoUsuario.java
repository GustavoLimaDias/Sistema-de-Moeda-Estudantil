package br.pucminas.moedaestudantil.security;

import br.pucminas.moedaestudantil.model.Perfil;

/** Usuário autenticado na requisição atual (recuperado a partir do token). */
public record SessaoUsuario(Long id, String nome, String email, Perfil perfil) {

    public boolean is(Perfil p) {
        return perfil == p;
    }
}
