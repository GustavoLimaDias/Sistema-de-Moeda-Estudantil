package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Superclasse de todos os perfis com acesso ao sistema (Aluno, Professor,
 * EmpresaParceira e Administrador). Mapeada com herança JOINED: a tabela
 * "usuario" guarda os dados comuns e cada subclasse tem sua própria tabela.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, unique = true, length = 60)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    /** Perfil de acesso definido por cada subclasse. */
    public abstract Perfil getPerfil();

    public boolean autenticar(String login, String senha, PasswordEncoder encoder) {
        return this.login.equals(login) && encoder.matches(senha, this.senhaHash);
    }

    public void alterarSenha(String novaSenha, PasswordEncoder encoder) {
        this.senhaHash = encoder.encode(novaSenha);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
}
