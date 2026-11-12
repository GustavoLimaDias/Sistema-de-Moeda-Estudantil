package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dto.EmpresaRequest;
import br.pucminas.moedaestudantil.dto.EmpresaResponse;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.repository.EmpresaParceiraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CRUD de empresa parceira (UC03 / US10). */
@Service
@Transactional
public class EmpresaParceiraService {

    private final EmpresaParceiraRepository empresaRepository;
    private final UsuarioService usuarioService;

    public EmpresaParceiraService(EmpresaParceiraRepository empresaRepository, UsuarioService usuarioService) {
        this.empresaRepository = empresaRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listar() {
        return empresaRepository.findAllByOrderByNomeAsc().stream().map(EmpresaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EmpresaResponse buscar(Long id) {
        return EmpresaResponse.from(buscarEntidade(id));
    }

    public EmpresaResponse cadastrar(EmpresaRequest request) {
        EmpresaParceira empresa = new EmpresaParceira();
        aplicar(empresa, null, request);
        return EmpresaResponse.from(empresaRepository.save(empresa));
    }

    public EmpresaResponse atualizar(Long id, EmpresaRequest request) {
        EmpresaParceira empresa = buscarEntidade(id);
        aplicar(empresa, id, request);
        return EmpresaResponse.from(empresaRepository.save(empresa));
    }

    /** Remove a empresa e, por composição, suas vantagens. */
    public void excluir(Long id) {
        EmpresaParceira empresa = buscarEntidade(id);
        empresaRepository.delete(empresa);
        empresaRepository.flush();
    }

    EmpresaParceira buscarEntidade(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa " + id + " não encontrada."));
    }

    private void aplicar(EmpresaParceira empresa, Long id, EmpresaRequest request) {
        String cnpj = Documentos.somenteDigitos(request.cnpj());
        if (!Documentos.cnpjValido(cnpj)) {
            throw new RegraNegocioException("cnpj", "CNPJ inválido.");
        }
        boolean cnpjEmUso = id == null ? empresaRepository.existsByCnpj(cnpj)
                : empresaRepository.existsByCnpjAndIdNot(cnpj, id);
        if (cnpjEmUso) {
            throw new RegraNegocioException("cnpj", "Já existe uma empresa com este CNPJ.");
        }

        usuarioService.aplicarDadosDeAcesso(empresa, id, request.nome(), request.email(), request.login(), request.senha());

        empresa.setCnpj(cnpj);
        empresa.setDescricao(request.descricao() == null || request.descricao().isBlank()
                ? null : request.descricao().trim());
    }
}
