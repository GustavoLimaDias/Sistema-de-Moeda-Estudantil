package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.NotificacaoEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoEmailRepository extends JpaRepository<NotificacaoEmail, Long> {

    List<NotificacaoEmail> findTop100ByOrderByDataHoraDesc();

    List<NotificacaoEmail> findByDestinatarioIgnoreCaseOrderByDataHoraDesc(String destinatario);
}
