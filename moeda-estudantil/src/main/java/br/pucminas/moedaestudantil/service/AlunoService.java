package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dto.AlunoRequest;
import br.pucminas.moedaestudantil.dto.AlunoResponse;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.ResgateVantagemRepository;
import br.pucminas.moedaestudantil.repository.TransferenciaMoedaRepository;
import br.pucminas.moedaestudantil.security.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CRUD de aluno (UC02 cadastro, UC15 perfil) e lista de alunos para o professor. */
@Service
@Transactional
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final UsuarioService usuarioService;
    private final InstituicaoService instituicaoService;
    private final ProfessorRepository professorRepository;
    private final TransferenciaMoedaRepository transferenciaRepository;
    private final ResgateVantagemRepository resgateRepository;
    private final TokenService tokenService;

    public AlunoService(AlunoRepository alunoRepository, UsuarioService usuarioService,
                        InstituicaoService instituicaoService, ProfessorRepository professorRepository,
                        TransferenciaMoedaRepository transferenciaRepository,
                        ResgateVantagemRepository resgateRepository, TokenService tokenService) {
        this.alunoRepository = alunoRepository;
        this.usuarioService = usuarioService;
        this.instituicaoService = instituicaoService;
        this.professorRepository = professorRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.resgateRepository = resgateRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public List<AlunoResponse> listar() {
        return alunoRepository.findAllByOrderByNomeAsc().stream().map(AlunoResponse::from).toList();
    }

    /** O professor só envia moedas para alunos da própria instituição. */
    @Transactional(readOnly = true)
    public List<AlunoResponse> listarDaInstituicaoDoProfessor(Long professorId) {
        Professor professor = professorRepository.findWithInstituicaoById(professorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado."));
        return alunoRepository.findByInstituicaoIdOrderByNomeAsc(professor.getInstituicao().getId()).stream()
                .map(AlunoResponse::from).toList();
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
        alunoRepository.save(aluno);
        tokenService.atualizarDados(aluno);
        return AlunoResponse.from(aluno);
    }

    /** Alunos com movimentações não são excluídos, para preservar o histórico (extratos e cupons). */
    public void excluir(Long id) {
        Aluno aluno = buscarEntidade(id);
        if (transferenciaRepository.existsByDestinatarioId(id) || resgateRepository.existsByAlunoId(id)) {
            throw new RegraNegocioException("Este aluno já recebeu ou trocou moedas e não pode ser excluído.");
        }
        alunoRepository.delete(aluno);
        alunoRepository.flush();
        tokenService.revogarDoUsuario(id);
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
