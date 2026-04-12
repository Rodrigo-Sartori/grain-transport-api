-- ============================================================
-- V1: Criação das tabelas do sistema grain-transport-api
-- ============================================================

CREATE TABLE IF NOT EXISTS filial (
    id      BIGSERIAL PRIMARY KEY,
    nome    VARCHAR(100) NOT NULL,
    cidade  VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS balanca (
    id        BIGSERIAL PRIMARY KEY,
    codigo    VARCHAR(50)  NOT NULL UNIQUE,
    api_key   VARCHAR(100) NOT NULL UNIQUE,
    filial_id BIGINT       NOT NULL REFERENCES filial(id)
);

CREATE TABLE IF NOT EXISTS caminhao (
    id    BIGSERIAL PRIMARY KEY,
    placa VARCHAR(20) NOT NULL UNIQUE,
    tara  DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS tipo_grao (
    id                  BIGSERIAL PRIMARY KEY,
    nome                VARCHAR(100)     NOT NULL,
    preco_por_tonelada  DOUBLE PRECISION NOT NULL,
    estoque_toneladas   DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS transacao_transporte (
    id           BIGSERIAL PRIMARY KEY,
    caminhao_id  BIGINT      NOT NULL REFERENCES caminhao(id),
    tipo_grao_id BIGINT      NOT NULL REFERENCES tipo_grao(id),
    filial_id    BIGINT      NOT NULL REFERENCES filial(id),
    iniciada_em  TIMESTAMP   NOT NULL DEFAULT now(),
    finalizada_em TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pesagem (
    id               BIGSERIAL PRIMARY KEY,
    transacao_id     BIGINT           NOT NULL REFERENCES transacao_transporte(id),
    balanca_id       BIGINT           NOT NULL REFERENCES balanca(id),
    peso_bruto       DOUBLE PRECISION NOT NULL,
    tara             DOUBLE PRECISION NOT NULL,
    peso_liquido     DOUBLE PRECISION NOT NULL,
    custo_carga      DOUBLE PRECISION NOT NULL,
    margem_aplicada  DOUBLE PRECISION NOT NULL,
    pesado_em        TIMESTAMP        NOT NULL DEFAULT now(),
    idempotency_key  VARCHAR(100)     NOT NULL UNIQUE
);

-- Índices de performance
CREATE INDEX idx_pesagem_transacao_id    ON pesagem(transacao_id);
CREATE INDEX idx_pesagem_pesado_em       ON pesagem(pesado_em);
CREATE INDEX idx_balanca_api_key         ON balanca(api_key);
CREATE INDEX idx_transacao_finalizada_em ON transacao_transporte(finalizada_em);
