package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.InstituicaoResponse;
import br.pucminas.moedaestudantil.service.InstituicaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Instituições pré-cadastradas, usadas no select do cadastro de aluno. */
@RestController
@RequestMapping("/api/instituicoes")
public class InstituicaoController {

    private final InstituicaoService service;

    public InstituicaoController(InstituicaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<InstituicaoResponse> listar() {
        return service.listar();
    }
}
