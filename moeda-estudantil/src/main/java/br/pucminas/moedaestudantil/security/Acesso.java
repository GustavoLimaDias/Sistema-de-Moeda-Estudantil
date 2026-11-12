package br.pucminas.moedaestudantil.security;

import br.pucminas.moedaestudantil.exception.AcessoNegadoException;
import br.pucminas.moedaestudantil.model.Perfil;

/** Regras de autorização sobre o próprio registro ("só o dono ou o administrador"). */
public final class Acesso {

    private Acesso() {
    }

    public static void exigirProprioOuAdmin(SessaoUsuario sessao, Long id) {
        if (sessao == null || !(sessao.is(Perfil.ADMIN) || sessao.id().equals(id))) {
            throw new AcessoNegadoException("Você só pode acessar os seus próprios dados.");
        }
    }
}
