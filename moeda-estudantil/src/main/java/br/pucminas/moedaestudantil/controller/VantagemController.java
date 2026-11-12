package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.CatalogoVantagemResponse;
import br.pucminas.moedaestudantil.service.VantagemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** UC07 – catálogo de vantagens (qualquer usuário autenticado). */
@RestController
@RequestMapping("/api/vantagens")
public class VantagemController {

    private final VantagemService vantagemService;

    public VantagemController(VantagemService vantagemService) {
        this.vantagemService = vantagemService;
    }

    @GetMapping
    public List<CatalogoVantagemResponse> catalogo() {
        return vantagemService.catalogo();
    }
}
