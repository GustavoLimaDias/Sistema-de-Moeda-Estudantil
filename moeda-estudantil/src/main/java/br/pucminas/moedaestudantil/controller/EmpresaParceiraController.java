package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.EmpresaRequest;
import br.pucminas.moedaestudantil.dto.EmpresaResponse;
import br.pucminas.moedaestudantil.dto.VantagemRequest;
import br.pucminas.moedaestudantil.dto.VantagemResponse;
import br.pucminas.moedaestudantil.service.EmpresaParceiraService;
import br.pucminas.moedaestudantil.service.VantagemService;
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

@RestController
@RequestMapping("/api/empresas")
public class EmpresaParceiraController {

    private final EmpresaParceiraService empresaService;
    private final VantagemService vantagemService;

    public EmpresaParceiraController(EmpresaParceiraService empresaService, VantagemService vantagemService) {
        this.empresaService = empresaService;
        this.vantagemService = vantagemService;
    }

    // ---------- Empresa ----------

    @GetMapping
    public List<EmpresaResponse> listar() {
        return empresaService.listar();
    }

    @GetMapping("/{id}")
    public EmpresaResponse buscar(@PathVariable Long id) {
        return empresaService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<EmpresaResponse> cadastrar(@Valid @RequestBody EmpresaRequest request) {
        EmpresaResponse criada = empresaService.cadastrar(request);
        return ResponseEntity.created(URI.create("/api/empresas/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    public EmpresaResponse atualizar(@PathVariable Long id, @Valid @RequestBody EmpresaRequest request) {
        return empresaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        empresaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Vantagens da empresa ----------

    @GetMapping("/{empresaId}/vantagens")
    public List<VantagemResponse> listarVantagens(@PathVariable Long empresaId) {
        return vantagemService.listarPorEmpresa(empresaId);
    }

    @PostMapping("/{empresaId}/vantagens")
    public ResponseEntity<VantagemResponse> cadastrarVantagem(@PathVariable Long empresaId,
                                                              @Valid @RequestBody VantagemRequest request) {
        VantagemResponse criada = vantagemService.cadastrar(empresaId, request);
        return ResponseEntity.created(URI.create("/api/empresas/" + empresaId + "/vantagens/" + criada.id()))
                .body(criada);
    }

    @PutMapping("/{empresaId}/vantagens/{vantagemId}")
    public VantagemResponse atualizarVantagem(@PathVariable Long empresaId, @PathVariable Long vantagemId,
                                              @Valid @RequestBody VantagemRequest request) {
        return vantagemService.atualizar(empresaId, vantagemId, request);
    }

    @DeleteMapping("/{empresaId}/vantagens/{vantagemId}")
    public ResponseEntity<Void> excluirVantagem(@PathVariable Long empresaId, @PathVariable Long vantagemId) {
        vantagemService.excluir(empresaId, vantagemId);
        return ResponseEntity.noContent().build();
    }
}
