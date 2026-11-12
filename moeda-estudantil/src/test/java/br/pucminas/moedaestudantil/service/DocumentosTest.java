package br.pucminas.moedaestudantil.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentosTest {

    @Test
    void validaCpf() {
        assertTrue(Documentos.cpfValido("52601815906"));
        assertFalse(Documentos.cpfValido("52601815907"));
        assertFalse(Documentos.cpfValido("11111111111"));
        assertFalse(Documentos.cpfValido("123"));
    }

    @Test
    void validaCnpj() {
        assertTrue(Documentos.cnpjValido("18625276000120"));
        assertFalse(Documentos.cnpjValido("18625276000121"));
        assertFalse(Documentos.cnpjValido("00000000000000"));
    }

    @Test
    void removeMascara() {
        assertEquals("52601815906", Documentos.somenteDigitos("526.018.159-06"));
    }
}
