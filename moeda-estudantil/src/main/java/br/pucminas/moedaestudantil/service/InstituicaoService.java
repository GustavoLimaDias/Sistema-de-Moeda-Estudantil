package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dto.InstituicaoRequest;
import br.pucminas.moedaestudantil.dto.InstituicaoResponse;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import br.pucminas.moedaestudantil.repository.InstituicaoEnsinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** UC13 – Instituições de ensino parceiras (mantidas pelo administrador). */
@Service
@Transactional(readOnly = true)
public class InstituicaoService {

    private final InstituicaoEnsinoRepository repository;

    public InstituicaoService(InstituicaoEnsinoRepository repository) {
        this.repository = repository;
    }

    public List<InstituicaoResponse> listar() {
        return repository.findAllByOrderByNomeAsc().stream().map(InstituicaoResponse::from).toList();
    }

    public InstituicaoEnsino buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("instituicaoId", "Instituição não encontrada."));
    }

    @Transactional
    public InstituicaoResponse cadastrar(InstituicaoRequest request) {
        InstituicaoEnsino instituicao = new InstituicaoEnsino();
        aplicar(instituicao, null, request);
        return InstituicaoResponse.from(repository.save(instituicao));
    }

    @Transactional
    public InstituicaoResponse atualizar(Long id, InstituicaoRequest request) {
        InstituicaoEnsino instituicao = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instituição " + id + " não encontrada."));
        aplicar(instituicao, id, request);
        return InstituicaoResponse.from(repository.save(instituicao));
    }

    private void aplicar(InstituicaoEnsino instituicao, Long id, InstituicaoRequest request) {
        String cnpj = Documentos.somenteDigitos(request.cnpj());
        if (!Documentos.cnpjValido(cnpj)) {
            throw new RegraNegocioException("cnpj", "CNPJ inválido.");
        }
        boolean emUso = id == null ? repository.existsByCnpj(cnpj) : repository.existsByCnpjAndIdNot(cnpj, id);
        if (emUso) {
            throw new RegraNegocioException("cnpj", "Já existe uma instituição com este CNPJ.");
        }
        instituicao.setNome(request.nome().trim());
        instituicao.setCnpj(cnpj);
    }
}
