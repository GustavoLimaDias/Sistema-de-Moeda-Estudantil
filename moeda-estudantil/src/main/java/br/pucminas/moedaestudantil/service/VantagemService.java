package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.dto.CatalogoVantagemResponse;
import br.pucminas.moedaestudantil.dto.VantagemRequest;
import br.pucminas.moedaestudantil.dto.VantagemResponse;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.Vantagem;
import br.pucminas.moedaestudantil.repository.ResgateVantagemRepository;
import br.pucminas.moedaestudantil.repository.VantagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Vantagens oferecidas por uma empresa parceira (UC11 / US11). */
@Service
@Transactional
public class VantagemService {

    private final VantagemRepository vantagemRepository;
    private final EmpresaParceiraService empresaService;
    private final ResgateVantagemRepository resgateRepository;

    public VantagemService(VantagemRepository vantagemRepository, EmpresaParceiraService empresaService,
                           ResgateVantagemRepository resgateRepository) {
        this.vantagemRepository = vantagemRepository;
        this.empresaService = empresaService;
        this.resgateRepository = resgateRepository;
    }

    /** UC07 – catálogo de vantagens ativas de todas as empresas, do menor para o maior custo. */
    @Transactional(readOnly = true)
    public List<CatalogoVantagemResponse> catalogo() {
        return vantagemRepository.findByAtivaTrueOrderByCustoAsc().stream()
                .map(CatalogoVantagemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<VantagemResponse> listarPorEmpresa(Long empresaId) {
        empresaService.buscarEntidade(empresaId);
        return vantagemRepository.findByEmpresaIdOrderByNomeAsc(empresaId).stream()
                .map(VantagemResponse::from).toList();
    }

    public VantagemResponse cadastrar(Long empresaId, VantagemRequest request) {
        EmpresaParceira empresa = empresaService.buscarEntidade(empresaId);
        Vantagem vantagem = new Vantagem();
        aplicar(vantagem, request);
        empresa.cadastrarVantagem(vantagem);
        return VantagemResponse.from(vantagemRepository.save(vantagem));
    }

    public VantagemResponse atualizar(Long empresaId, Long vantagemId, VantagemRequest request) {
        Vantagem vantagem = buscarEntidade(empresaId, vantagemId);
        aplicar(vantagem, request);
        return VantagemResponse.from(vantagemRepository.save(vantagem));
    }

    public void excluir(Long empresaId, Long vantagemId) {
        Vantagem vantagem = buscarEntidade(empresaId, vantagemId);
        if (resgateRepository.existsByVantagemId(vantagemId)) {
            throw new RegraNegocioException("Esta vantagem já foi resgatada por alunos. Desative-a em vez de excluir.");
        }
        vantagem.getEmpresa().removerVantagem(vantagem);
        vantagemRepository.delete(vantagem);
        vantagemRepository.flush();
    }

    private Vantagem buscarEntidade(Long empresaId, Long vantagemId) {
        return vantagemRepository.findByIdAndEmpresaId(vantagemId, empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vantagem " + vantagemId + " não encontrada."));
    }

    private void aplicar(Vantagem vantagem, VantagemRequest request) {
        vantagem.setNome(request.nome().trim());
        vantagem.setDescricao(request.descricao().trim());
        vantagem.setFotoUrl(request.fotoUrl().trim());
        vantagem.setCusto(request.custo());
        vantagem.setAtiva(request.ativa() == null || request.ativa());
    }
}
