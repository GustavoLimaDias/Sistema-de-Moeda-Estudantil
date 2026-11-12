package br.pucminas.moedaestudantil.exception;

import java.util.Map;

/** Violação de regra de negócio (ex.: CPF já cadastrado). Pode apontar o campo do formulário. */
public class RegraNegocioException extends RuntimeException {

    private final String campo;

    public RegraNegocioException(String mensagem) {
        this(null, mensagem);
    }

    public RegraNegocioException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public Map<String, String> getCampos() {
        return campo == null ? Map.of() : Map.of(campo, getMessage());
    }
}
