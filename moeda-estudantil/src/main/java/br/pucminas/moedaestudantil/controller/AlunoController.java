package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.AlunoRequest;
import br.pucminas.moedaestudantil.dto.AlunoResponse;
import br.pucminas.moedaestudantil.dto.ExtratoResponse;
import br.pucminas.moedaestudantil.dto.ResgateRequest;
import br.pucminas.moedaestudantil.dto.ResgateResponse;
import br.pucminas.moedaestudantil.model.Perfil;
import br.pucminas.moedaestudantil.security.Acesso;
import br.pucminas.moedaestudantil.security.Publico;
import br.pucminas.moedaestudantil.security.RequerPerfil;
import br.pucminas.moedaestudantil.security.SessaoUsuario;
import br.pucminas.moedaestudantil.security.UsuarioLogado;
import br.pucminas.moedaestudantil.service.AlunoService;
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
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;
    private final TransacaoService transacaoService;

    public AlunoController(AlunoService alunoService, TransacaoService transacaoService) {
        this.alunoService = alunoService;
        this.transacaoService = transacaoService;
    }

    // ---------- CRUD ----------

    /** Administrador vê todos; professor vê só os alunos da sua instituição (para enviar moedas). */
    @GetMapping
    @RequerPerfil({Perfil.ADMIN, Perfil.PROFESSOR})
    public List<AlunoResponse> listar(@UsuarioLogado SessaoUsuario sessao) {
        return sessao.is(Perfil.PROFESSOR)
                ? alunoService.listarDaInstituicaoDoProfessor(sessao.id())
                : alunoService.listar();
    }

    @GetMapping("/{id}")
    public AlunoResponse buscar(@PathVariable Long id, @UsuarioLogado SessaoUsuario sessao) {
        Acesso.exigirProprioOuAdmin(sessao, id);
        return alunoService.buscar(id);
    }

    /** UC02 – cadastro do aluno (público). */
    @Publico
    @PostMapping
    public ResponseEntity<AlunoResponse> cadastrar(@Valid @RequestBody AlunoRequest request) {
        AlunoResponse criado = alunoService.cadastrar(request);
        return ResponseEntity.created(URI.create("/api/alunos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public AlunoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AlunoRequest request,
                                   @UsuarioLogado SessaoUsuario sessao) {
        Acesso.exigirProprioOuAdmin(sessao, id);
        return alunoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @UsuarioLogado SessaoUsuario sessao) {
        Acesso.exigirProprioOuAdmin(sessao, id);
        alunoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- área do aluno ----------

    /** UC04 – extrato: saldo, moedas recebidas e trocas realizadas. */
    @GetMapping("/me/extrato")
    @RequerPerfil(Perfil.ALUNO)
    public ExtratoResponse extrato(@UsuarioLogado SessaoUsuario sessao) {
        return transacaoService.extratoAluno(sessao.id());
    }

    /** UC08 – resgatar vantagem (gera cupom e emails). */
    @PostMapping("/me/resgates")
    @RequerPerfil(Perfil.ALUNO)
    public ResponseEntity<ResgateResponse> resgatar(@Valid @RequestBody ResgateRequest request,
                                                    @UsuarioLogado SessaoUsuario sessao) {
        ResgateResponse resgate = transacaoService.resgatarVantagem(sessao.id(), request.vantagemId());
        return ResponseEntity.status(201).body(resgate);
    }

    /** UC17 – cancelar um cupom ainda não usado; as moedas voltam para o saldo. */
    @PostMapping("/me/resgates/{codigo}/cancelar")
    @RequerPerfil(Perfil.ALUNO)
    public ResgateResponse cancelarResgate(@PathVariable String codigo, @UsuarioLogado SessaoUsuario sessao) {
        return transacaoService.cancelarResgateDoAluno(sessao.id(), codigo);
    }
}
