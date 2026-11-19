package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.InstituicaoRequest;
import br.pucminas.moedaestudantil.dto.InstituicaoResponse;
import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.security.Publico;
import br.pucminas.moedaestudantil.security.RequerPerfil;
import br.pucminas.moedaestudantil.service.InstituicaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/** UC13 – instituições pré-cadastradas. A lista é pública porque alimenta o cadastro do aluno. */
@RestController
@RequestMapping("/api/instituicoes")
public class InstituicaoController {

    private final InstituicaoService service;

    public InstituicaoController(InstituicaoService service) {
        this.service = service;
    }

    @Publico
    @GetMapping
    public List<InstituicaoResponse> listar() {
        return service.listar();
    }

    @PostMapping
    @RequerPerfil(Perfil.ADMIN)
    public ResponseEntity<InstituicaoResponse> cadastrar(@Valid @RequestBody InstituicaoRequest request) {
        InstituicaoResponse criada = service.cadastrar(request);
        return ResponseEntity.created(URI.create("/api/instituicoes/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    @RequerPerfil(Perfil.ADMIN)
    public InstituicaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody InstituicaoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @RequerPerfil(Perfil.ADMIN)
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
