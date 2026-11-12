package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/** Responsável por manter instituições e importar professores. */
@Entity
@Table(name = "administrador")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Administrador extends Usuario {
}
