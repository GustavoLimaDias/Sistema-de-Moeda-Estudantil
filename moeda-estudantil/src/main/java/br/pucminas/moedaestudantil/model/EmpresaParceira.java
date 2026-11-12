package br.pucminas.moedaestudantil.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresa_parceira")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class EmpresaParceira extends Usuario {

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(length = 500)
    private String descricao;

    /** Composição: as vantagens pertencem à empresa e são removidas com ela. */
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nome ASC")
    private List<Vantagem> vantagens = new ArrayList<>();

    public void cadastrarVantagem(Vantagem vantagem) {
        vantagem.setEmpresa(this);
        vantagens.add(vantagem);
    }

    public void removerVantagem(Vantagem vantagem) {
        vantagens.remove(vantagem);
        vantagem.setEmpresa(null);
    }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public List<Vantagem> getVantagens() { return vantagens; }
}
