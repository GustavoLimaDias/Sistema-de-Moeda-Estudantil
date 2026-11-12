-- =====================================================================
-- Sistema de Moeda Estudantil - Modelo físico (PostgreSQL)
-- Lab03S02 - Projeto de Software - PUC Minas
--
-- Esquema equivalente ao gerado pelo Hibernate a partir das entidades JPA
-- (spring.jpa.hibernate.ddl-auto=update). As restrições CHECK documentam
-- regras que a aplicação também valida. Herança mapeada com JOINED:
--   usuario   -> administrador, aluno, professor, empresa_parceira
--   transacao -> transferencia_moeda, resgate_vantagem, credito_semestral
-- =====================================================================

CREATE TABLE instituicao_ensino (
    id      BIGSERIAL    PRIMARY KEY,
    nome    VARCHAR(150) NOT NULL,
    cnpj    CHAR(14)     NOT NULL UNIQUE
);

CREATE TABLE usuario (
    id          BIGSERIAL    PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(120) NOT NULL UNIQUE,
    login       VARCHAR(60)  NOT NULL UNIQUE,
    senha_hash  VARCHAR(100) NOT NULL
);

CREATE TABLE administrador (
    usuario_id  BIGINT PRIMARY KEY REFERENCES usuario (id)
);

CREATE TABLE aluno (
    usuario_id      BIGINT       PRIMARY KEY REFERENCES usuario (id),
    cpf             CHAR(11)     NOT NULL UNIQUE,
    rg              VARCHAR(20)  NOT NULL,
    curso           VARCHAR(100) NOT NULL,
    saldo           INT          NOT NULL DEFAULT 0 CHECK (saldo >= 0),
    -- endereço (objeto embutido @Embeddable Endereco)
    logradouro      VARCHAR(150) NOT NULL,
    numero          VARCHAR(10)  NOT NULL,
    complemento     VARCHAR(60),
    bairro          VARCHAR(80)  NOT NULL,
    cidade          VARCHAR(80)  NOT NULL,
    uf              CHAR(2)      NOT NULL,
    cep             CHAR(8)      NOT NULL,
    instituicao_id  BIGINT       NOT NULL REFERENCES instituicao_ensino (id)
);

CREATE TABLE professor (
    usuario_id      BIGINT       PRIMARY KEY REFERENCES usuario (id),
    cpf             CHAR(11)     NOT NULL UNIQUE,
    departamento    VARCHAR(100) NOT NULL,
    saldo           INT          NOT NULL DEFAULT 0 CHECK (saldo >= 0),
    instituicao_id  BIGINT       NOT NULL REFERENCES instituicao_ensino (id)
);

CREATE TABLE empresa_parceira (
    usuario_id  BIGINT       PRIMARY KEY REFERENCES usuario (id),
    cnpj        CHAR(14)     NOT NULL UNIQUE,
    descricao   VARCHAR(500)
);

CREATE TABLE vantagem (
    id          BIGSERIAL     PRIMARY KEY,
    nome        VARCHAR(120)  NOT NULL,
    descricao   VARCHAR(1000) NOT NULL,
    foto_url    VARCHAR(500)  NOT NULL,
    custo       INT           NOT NULL CHECK (custo > 0),
    ativa       BOOLEAN       NOT NULL DEFAULT TRUE,
    empresa_id  BIGINT        NOT NULL REFERENCES empresa_parceira (usuario_id)  -- exclusão em cascata feita pelo JPA (composição)
);

CREATE TABLE transacao (
    id         BIGSERIAL PRIMARY KEY,
    data_hora  TIMESTAMP NOT NULL,
    valor      INT       NOT NULL CHECK (valor > 0)
);

CREATE TABLE transferencia_moeda (
    transacao_id  BIGINT       PRIMARY KEY REFERENCES transacao (id),
    motivo        VARCHAR(500) NOT NULL,
    professor_id  BIGINT       NOT NULL REFERENCES professor (usuario_id),
    aluno_id      BIGINT       NOT NULL REFERENCES aluno (usuario_id)
);

CREATE TABLE resgate_vantagem (
    transacao_id  BIGINT      PRIMARY KEY REFERENCES transacao (id),
    codigo_cupom  VARCHAR(12) NOT NULL UNIQUE,
    status        VARCHAR(20) NOT NULL CHECK (status IN ('PENDENTE', 'CONFERIDO', 'CANCELADO')),
    aluno_id      BIGINT      NOT NULL REFERENCES aluno (usuario_id),
    vantagem_id   BIGINT      NOT NULL REFERENCES vantagem (id)
);

CREATE TABLE credito_semestral (
    transacao_id  BIGINT     PRIMARY KEY REFERENCES transacao (id),
    semestre      VARCHAR(6) NOT NULL,
    professor_id  BIGINT     NOT NULL REFERENCES professor (usuario_id),
    CONSTRAINT uk_credito_professor_semestre UNIQUE (professor_id, semestre)
);

-- Índices para as consultas mais frequentes (listas e extratos)
CREATE INDEX idx_aluno_instituicao        ON aluno (instituicao_id);
CREATE INDEX idx_professor_instituicao    ON professor (instituicao_id);
CREATE INDEX idx_vantagem_empresa         ON vantagem (empresa_id);
CREATE INDEX idx_transferencia_aluno      ON transferencia_moeda (aluno_id);
CREATE INDEX idx_transferencia_professor  ON transferencia_moeda (professor_id);
CREATE INDEX idx_resgate_aluno            ON resgate_vantagem (aluno_id);
