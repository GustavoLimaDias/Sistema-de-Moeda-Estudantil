package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dto.InstituicaoResponse;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import br.pucminas.moedaestudantil.repository.InstituicaoEnsinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
