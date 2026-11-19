package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.EnvioMoedasRequest;
import br.pucminas.moedaestudantil.dto.ExtratoResponse;
import br.pucminas.moedaestudantil.dto.ProfessorRequest;
import br.pucminas.moedaestudantil.dto.ProfessorResponse;
import br.pucminas.moedaestudantil.dto.TransferenciaResponse;
import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.security.Acesso;
import br.pucminas.moedaestudantil.security.RequerPerfil;
import br.pucminas.moedaestudantil.security.SessaoUsuario;
import br.pucminas.moedaestudantil.security.UsuarioLogado;
import br.pucminas.moedaestudantil.service.ProfessorService;
import br.pucminas.moedaestudantil.service.TransacaoService;
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
@RequestMapping("/api/professores")
public class ProfessorController {

    private final ProfessorService professorService;
    private final TransacaoService transacaoService;

    public ProfessorController(ProfessorService professorService, TransacaoService transacaoService) {
        this.professorService = professorService;
        this.transacaoService = transacaoService;
    }

    // ---------- UC14 – pré-cadastro (administrador) ----------

    @GetMapping
    @RequerPerfil(Perfil.ADMIN)
    public List<ProfessorResponse> listar() {
        return professorService.listar();
    }

    @GetMapping("/{id}")
    public ProfessorResponse buscar(@PathVariable Long id, @UsuarioLogado SessaoUsuario sessao) {
        Acesso.exigirProprioOuAdmin(sessao, id);
        return professorService.buscar(id);
    }

    @PostMapping
    @RequerPerfil(Perfil.ADMIN)
    public ResponseEntity<ProfessorResponse> cadastrar(@Valid @RequestBody ProfessorRequest request) {
        ProfessorResponse criado = professorService.cadastrar(request);
        return ResponseEntity.created(URI.create("/api/professores/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @RequerPerfil(Perfil.ADMIN)
    public ProfessorResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProfessorRequest request) {
        return professorService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @RequerPerfil(Perfil.ADMIN)
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        professorService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** Importa a lista enviada pela instituição (tudo ou nada). */
    @PostMapping("/importacao")
    @RequerPerfil(Perfil.ADMIN)
    public ResponseEntity<List<ProfessorResponse>> importar(@RequestBody List<ProfessorRequest> lista) {
        return ResponseEntity.status(201).body(professorService.importar(lista));
    }

    // ---------- área do professor ----------

    /** UC05 – enviar moedas a um aluno (motivo obrigatório; notifica o aluno por email). */
    @PostMapping("/me/envios")
    @RequerPerfil(Perfil.PROFESSOR)
    public ResponseEntity<TransferenciaResponse> enviar(@Valid @RequestBody EnvioMoedasRequest request,
                                                        @UsuarioLogado SessaoUsuario sessao) {
        return ResponseEntity.status(201).body(transacaoService.enviarMoedas(sessao.id(), request));
    }

    /** UC04 – extrato do professor: saldo, envios e créditos semestrais. */
    @GetMapping("/me/extrato")
    @RequerPerfil(Perfil.PROFESSOR)
    public ExtratoResponse extrato(@UsuarioLogado SessaoUsuario sessao) {
        return transacaoService.extratoProfessor(sessao.id());
    }
}
