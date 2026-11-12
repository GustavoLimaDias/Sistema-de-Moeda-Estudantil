package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.security.SessaoUsuario;

public record LoginResponse(String token, SessaoUsuario usuario) {
}
