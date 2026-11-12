package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.EmpresaRequest;
import br.pucminas.moedaestudantil.dto.EmpresaResponse;
import br.pucminas.moedaestudantil.dto.ResgateResponse;
import br.pucminas.moedaestudantil.dto.VantagemRequest;
import br.pucminas.moedaestudantil.dto.VantagemResponse;
import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.security.Acesso;
import br.pucminas.moedaestudantil.security.Publico;
import br.pucminas.moedaestudantil.security.RequerPerfil;
import br.pucminas.moedaestudantil.security.SessaoUsuario;
import br.pucminas.moedaestudantil.security.UsuarioLogado;
import br.pucminas.moedaestudantil.service.EmpresaParceiraService;
import br.pucminas.moedaestudantil.service.TransacaoService;
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
    private final TransacaoService transacaoService;

    public EmpresaParceiraController(EmpresaParceiraService empresaService, VantagemService vantagemService,
                                     TransacaoService transacaoService) {
        this.empresaService = empresaService;
        this.vantagemService = vantagemService;
        this.transacaoService = transacaoService;
    }

    // ---------- CRUD da empresa ----------

    @GetMapping
    @RequerPerfil(Perfil.ADMIN)
    public List<EmpresaResponse> listar() {
        return empresaService.listar();
    }

    @GetMapping("/{id}")
    public EmpresaResponse buscar(@PathVariable Long id, @UsuarioLogado SessaoUsuario sessao) {
        Acesso.exigirProprioOuAdmin(sessao, id);
        return empresaService.buscar(id);
    }

    /** UC03 – cadastro da empresa parceira (público). */
    @Publico
    @PostMapping
    public ResponseEntity<EmpresaResponse> cadastrar(@Valid @RequestBody EmpresaRequest request) {
        EmpresaResponse criada = empresaService.cadastrar(request);
        return ResponseEntity.created(URI.create("/api/empresas/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    public EmpresaResponse atualizar(@PathVariable Long id, @Valid @RequestBody EmpresaRequest request,
                                     @UsuarioLogado SessaoUsuario sessao) {
        Acesso.exigirProprioOuAdmin(sessao, id);
        return empresaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @UsuarioLogado SessaoUsuario sessao) {
        Acesso.exigirProprioOuAdmin(sessao, id);
        empresaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- UC11 – vantagens da empresa ----------

    @GetMapping("/{empresaId}/vantagens")
    public List<VantagemResponse> listarVantagens(@PathVariable Long empresaId, @UsuarioLogado SessaoUsuario sessao) {
        Acesso.exigirProprioOuAdmin(sessao, empresaId);
        return vantagemService.listarPorEmpresa(empresaId);
    }

    @PostMapping("/{empresaId}/vantagens")
    public ResponseEntity<VantagemResponse> cadastrarVantagem(@PathVariable Long empresaId,
                                                              @Valid @RequestBody VantagemRequest request,
                                                              @UsuarioLogado SessaoUsuario sessao) {
        Acesso.exigirProprioOuAdmin(sessao, empresaId);
        VantagemResponse criada = vantagemService.cadastrar(empresaId, request);
        return ResponseEntity.created(URI.create("/api/empresas/" + empresaId + "/vantagens/" + criada.id()))
                .body(criada);
    }

    @PutMapping("/{empresaId}/vantagens/{vantagemId}")
    public VantagemResponse atualizarVantagem(@PathVariable Long empresaId, @PathVariable Long vantagemId,
                                              @Valid @RequestBody VantagemRequest request,
                                              @UsuarioLogado SessaoUsuario sessao) {
        Acesso.exigirProprioOuAdmin(sessao, empresaId);
        return vantagemService.atualizar(empresaId, vantagemId, request);
    }

    @DeleteMapping("/{empresaId}/vantagens/{vantagemId}")
    public ResponseEntity<Void> excluirVantagem(@PathVariable Long empresaId, @PathVariable Long vantagemId,
                                                @UsuarioLogado SessaoUsuario sessao) {
        Acesso.exigirProprioOuAdmin(sessao, empresaId);
        vantagemService.excluir(empresaId, vantagemId);
        return ResponseEntity.noContent().build();
    }

    // ---------- conferência dos cupons ----------

    @GetMapping("/me/resgates")
    @RequerPerfil(Perfil.EMPRESA)
    public List<ResgateResponse> resgates(@UsuarioLogado SessaoUsuario sessao) {
        return transacaoService.resgatesDaEmpresa(sessao.id());
    }

    @PostMapping("/me/resgates/{codigo}/conferir")
    @RequerPerfil(Perfil.EMPRESA)
    public ResgateResponse conferir(@PathVariable String codigo, @UsuarioLogado SessaoUsuario sessao) {
        return transacaoService.conferirCupom(sessao.id(), codigo);
    }
}
