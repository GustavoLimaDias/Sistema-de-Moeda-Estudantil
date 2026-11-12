package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dto.AlunoRequest;
import br.pucminas.moedaestudantil.dto.AlunoResponse;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CRUD de aluno (UC02 / US01, US15). */
@Service
@Transactional
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final UsuarioService usuarioService;
    private final InstituicaoService instituicaoService;

    public AlunoService(AlunoRepository alunoRepository, UsuarioService usuarioService,
                        InstituicaoService instituicaoService) {
        this.alunoRepository = alunoRepository;
        this.usuarioService = usuarioService;
        this.instituicaoService = instituicaoService;
    }

    @Transactional(readOnly = true)
    public List<AlunoResponse> listar() {
        return alunoRepository.findAllByOrderByNomeAsc().stream().map(AlunoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AlunoResponse buscar(Long id) {
        return AlunoResponse.from(buscarEntidade(id));
    }

    public AlunoResponse cadastrar(AlunoRequest request) {
        Aluno aluno = new Aluno();
        aplicar(aluno, null, request);
        return AlunoResponse.from(alunoRepository.save(aluno));
    }

    public AlunoResponse atualizar(Long id, AlunoRequest request) {
        Aluno aluno = buscarEntidade(id);
        aplicar(aluno, id, request);
        return AlunoResponse.from(alunoRepository.save(aluno));
    }

    public void excluir(Long id) {
        Aluno aluno = buscarEntidade(id);
        alunoRepository.delete(aluno);
        alunoRepository.flush();
    }

    private Aluno buscarEntidade(Long id) {
        return alunoRepository.findWithInstituicaoById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno " + id + " não encontrado."));
    }

    private void aplicar(Aluno aluno, Long id, AlunoRequest request) {
        String cpf = Documentos.somenteDigitos(request.cpf());
        if (!Documentos.cpfValido(cpf)) {
            throw new RegraNegocioException("cpf", "CPF inválido.");
        }
        boolean cpfEmUso = id == null ? alunoRepository.existsByCpf(cpf) : alunoRepository.existsByCpfAndIdNot(cpf, id);
        if (cpfEmUso) {
            throw new RegraNegocioException("cpf", "Já existe um aluno com este CPF.");
        }

        usuarioService.aplicarDadosDeAcesso(aluno, id, request.nome(), request.email(), request.login(), request.senha());

        aluno.setCpf(cpf);
        aluno.setRg(request.rg().trim());
        aluno.setCurso(request.curso().trim());
        aluno.setEndereco(request.endereco().toEntity());
        aluno.setInstituicao(instituicaoService.buscarEntidade(request.instituicaoId()));
    }
}
