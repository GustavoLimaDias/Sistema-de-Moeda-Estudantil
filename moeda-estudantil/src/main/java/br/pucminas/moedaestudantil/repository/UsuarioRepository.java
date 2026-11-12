package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Unicidade de email e login vale para todos os perfis, por isso fica na superclasse. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByLogin(String login);

    boolean existsByLoginAndIdNot(String login, Long id);

    Optional<Usuario> findByLogin(String login);
}
