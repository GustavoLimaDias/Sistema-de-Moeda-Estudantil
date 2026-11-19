package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dto.ProfessorRequest;
import br.pucminas.moedaestudantil.dto.ProfessorResponse;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.repository.CreditoSemestralRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.TransferenciaMoedaRepository;
import br.pucminas.moedaestudantil.security.TokenService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * UC14 – Gerenciar professores: pré-cadastro (individual ou lista da instituição), edição e exclusão.
 * Se nenhuma senha for informada, a senha inicial é o CPF (só dígitos).
 */
@Service
@Transactional
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final UsuarioService usuarioService;
    private final InstituicaoService instituicaoService;
    private final CreditoSemestralService creditoSemestralService;
    private final Validator validator;
    private final TransferenciaMoedaRepository transferenciaRepository;
    private final CreditoSemestralRepository creditoRepository;
    private final TokenService tokenService;

    public ProfessorService(ProfessorRepository professorRepository, UsuarioService usuarioService,
                            InstituicaoService instituicaoService, CreditoSemestralService creditoSemestralService,
                            Validator validator, TransferenciaMoedaRepository transferenciaRepository,
                            CreditoSemestralRepository creditoRepository, TokenService tokenService) {
        this.validator = validator;
        this.transferenciaRepository = transferenciaRepository;
        this.creditoRepository = creditoRepository;
        this.tokenService = tokenService;
        this.professorRepository = professorRepository;
        this.usuarioService = usuarioService;
        this.instituicaoService = instituicaoService;
        this.creditoSemestralService = creditoSemestralService;
    }

    @Transactional(readOnly = true)
    public List<ProfessorResponse> listar() {
        return professorRepository.findAllByOrderByNomeAsc().stream().map(ProfessorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProfessorResponse buscar(Long id) {
        return ProfessorResponse.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Professor buscarEntidade(Long id) {
        return professorRepository.findWithInstituicaoById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor " + id + " não encontrado."));
    }

    public ProfessorResponse cadastrar(ProfessorRequest request) {
        return ProfessorResponse.from(criar(request));
    }

    /** Importação em lote: ou todos os professores da lista são cadastrados, ou nenhum. */
    public List<ProfessorResponse> importar(List<ProfessorRequest> lista) {
        if (lista == null || lista.isEmpty()) {
            throw new RegraNegocioException("A lista de professores está vazia.");
        }
        Set<String> cpfs = new HashSet<>();
        for (int i = 0; i < lista.size(); i++) {
            ProfessorRequest item = lista.get(i);
            if (item == null) {
                throw new RegraNegocioException("Linha " + (i + 1) + " da lista está vazia.");
            }
            Set<ConstraintViolation<ProfessorRequest>> erros = validator.validate(item);
            if (!erros.isEmpty()) {
                ConstraintViolation<ProfessorRequest> erro = erros.iterator().next();
                throw new RegraNegocioException("Linha " + (i + 1) + ": " + erro.getMessage() + ".");
            }
            if (!cpfs.add(Documentos.somenteDigitos(lista.get(i).cpf()))) {
                throw new RegraNegocioException("CPF repetido na lista (linha " + (i + 1) + ").");
            }
        }
        return lista.stream().map(this::criarComLinha).map(ProfessorResponse::from).toList();
    }

    /** Edição pelo administrador. Senha vazia mantém a atual. */
    public ProfessorResponse atualizar(Long id, ProfessorRequest request) {
        Professor professor = buscarEntidade(id);
        String cpf = Documentos.somenteDigitos(request.cpf());
        if (!Documentos.cpfValido(cpf)) {
            throw new RegraNegocioException("cpf", "CPF inválido.");
        }
        if (professorRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new RegraNegocioException("cpf", "Já existe um professor com este CPF.");
        }
        usuarioService.aplicarDadosDeAcesso(professor, id, request.nome(), request.email(), request.login(),
                request.senha());
        professor.setCpf(cpf);
        professor.setDepartamento(request.departamento().trim());
        professor.setInstituicao(instituicaoService.buscarEntidade(request.instituicaoId()));
        tokenService.atualizarDados(professor);
        return ProfessorResponse.from(professor);
    }

    /** Professor que já enviou moedas fica no histórico; os créditos semestrais saem junto com ele. */
    public void excluir(Long id) {
        Professor professor = buscarEntidade(id);
        if (transferenciaRepository.existsByRemetenteId(id)) {
            throw new RegraNegocioException("Este professor já enviou moedas e não pode ser excluído.");
        }
        creditoRepository.deleteAll(creditoRepository.findByProfessorIdOrderByDataHoraDesc(id));
        professorRepository.delete(professor);
        professorRepository.flush();
        tokenService.revogarDoUsuario(id);
    }

    private Professor criarComLinha(ProfessorRequest request) {
        try {
            return criar(request);
        } catch (RegraNegocioException e) {
            throw new RegraNegocioException("Professor " + request.nome() + ": " + e.getMessage());
        }
    }

    private Professor criar(ProfessorRequest request) {
        String cpf = Documentos.somenteDigitos(request.cpf());
        if (!Documentos.cpfValido(cpf)) {
            throw new RegraNegocioException("cpf", "CPF inválido.");
        }
        if (professorRepository.existsByCpf(cpf)) {
            throw new RegraNegocioException("cpf", "Já existe um professor com este CPF.");
        }
        String senha = request.senha() == null || request.senha().isBlank() ? cpf : request.senha();

        Professor professor = new Professor();
        usuarioService.aplicarDadosDeAcesso(professor, null, request.nome(), request.email(), request.login(), senha);
        professor.setCpf(cpf);
        professor.setDepartamento(request.departamento().trim());
        professor.setInstituicao(instituicaoService.buscarEntidade(request.instituicaoId()));
        professorRepository.save(professor);
        // professor que entra no meio do semestre já recebe as moedas do semestre corrente
        creditoSemestralService.creditarSeNecessario(professor.getId(),
                CreditoSemestralService.semestreDe(LocalDate.now()));
        return professor;
    }
}
