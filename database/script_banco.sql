-- =====================================================================
-- FoodLink - Script de criação do banco de dados
-- SGBD: PostgreSQL 12+ (compatível também com H2 em modo PostgreSQL,
--       usado apenas nos testes automatizados)
-- Execução:  psql -U postgres -d foodlink -f database/script_banco.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- Limpeza (ordem inversa das dependências) — permite reexecutar o script
-- ---------------------------------------------------------------------
DROP VIEW IF EXISTS vw_historico_doacoes;
DROP TABLE IF EXISTS indicador_impacto;
DROP TABLE IF EXISTS retirada;
DROP TABLE IF EXISTS reserva;
DROP TABLE IF EXISTS oferta_doacao;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS endereco;

-- ---------------------------------------------------------------------
-- ENDERECO  (classe Endereco)
-- ---------------------------------------------------------------------
CREATE TABLE endereco (
    id         UUID PRIMARY KEY,
    rua        VARCHAR(150) NOT NULL,
    cidade     VARCHAR(100) NOT NULL,
    cep        VARCHAR(10)  NOT NULL,
    latitude   DOUBLE PRECISION NOT NULL,
    longitude  DOUBLE PRECISION NOT NULL
);

-- ---------------------------------------------------------------------
-- USUARIO  (classe abstrata Usuario + herança de Doador/Receptor/Voluntário)
-- Estratégia: tabela única com discriminador "tipo" (single table inheritance).
-- limite_reservas_ativas só é usado quando tipo = 'RECEPTOR' (RN02).
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id                     UUID PRIMARY KEY,
    tipo                   VARCHAR(20)  NOT NULL
                           CHECK (tipo IN ('DOADOR', 'RECEPTOR', 'VOLUNTARIO')),
    nome                   VARCHAR(120) NOT NULL,
    email                  VARCHAR(150) NOT NULL UNIQUE,
    documento              VARCHAR(30)  NOT NULL UNIQUE,
    senha_hash             VARCHAR(128) NOT NULL,
    data_cadastro          TIMESTAMP    NOT NULL,
    limite_reservas_ativas INTEGER
);

CREATE INDEX idx_usuario_tipo ON usuario (tipo);

-- ---------------------------------------------------------------------
-- OFERTA_DOACAO  (entidade principal do CRUD)
--   doador_id  -> composição Doador (1) : (0..*) OfertaDoacao  ["publica"]
--   endereco_id-> associação  OfertaDoacao (1) : (1) Endereco  ["possui"]
-- ---------------------------------------------------------------------
CREATE TABLE oferta_doacao (
    id            UUID PRIMARY KEY,
    doador_id     UUID NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    endereco_id   UUID NOT NULL REFERENCES endereco (id),
    alimento      VARCHAR(120) NOT NULL,
    quantidade    NUMERIC(10, 2) NOT NULL CHECK (quantidade > 0),
    peso          NUMERIC(10, 2) NOT NULL CHECK (peso > 0),
    data_validade TIMESTAMP NOT NULL,
    observacoes   VARCHAR(255),
    status        VARCHAR(20) NOT NULL
                  CHECK (status IN ('DISPONIVEL', 'RESERVADA', 'CONCLUIDA', 'EXPIRADA'))
);

CREATE INDEX idx_oferta_status ON oferta_doacao (status);
CREATE INDEX idx_oferta_doador ON oferta_doacao (doador_id);
CREATE INDEX idx_oferta_validade ON oferta_doacao (data_validade);

-- ---------------------------------------------------------------------
-- RESERVA  (RF04)
--   oferta_id  -> OfertaDoacao (1) : (0..1) Reserva  ["reservada por"]
--   receptor_id-> Receptor     (1) : (0..*) Reserva   ["realiza"]
-- RN03: apenas uma reserva ATIVA por oferta (regra garantida no ReservaService;
--       no PostgreSQL pode ser reforçada com o índice único parcial abaixo).
-- ---------------------------------------------------------------------
CREATE TABLE reserva (
    id           UUID PRIMARY KEY,
    oferta_id    UUID NOT NULL REFERENCES oferta_doacao (id) ON DELETE CASCADE,
    receptor_id  UUID NOT NULL REFERENCES usuario (id),
    data_reserva TIMESTAMP NOT NULL,
    status       VARCHAR(20) NOT NULL
                 CHECK (status IN ('ATIVA', 'CANCELADA', 'CONCLUIDA'))
);

CREATE INDEX idx_reserva_oferta ON reserva (oferta_id);
CREATE INDEX idx_reserva_receptor_status ON reserva (receptor_id, status);

-- Reforço opcional da RN03 (habilitar apenas em PostgreSQL):
-- CREATE UNIQUE INDEX uk_reserva_ativa ON reserva (oferta_id) WHERE status = 'ATIVA';

-- ---------------------------------------------------------------------
-- RETIRADA  (RF05)
--   reserva_id   -> Reserva    (1) : (0..1) Retirada  ["gera"]
--   voluntario_id-> Voluntario (1) : (0..*) Retirada  ["executa"]
-- ---------------------------------------------------------------------
CREATE TABLE retirada (
    id            UUID PRIMARY KEY,
    reserva_id    UUID NOT NULL UNIQUE REFERENCES reserva (id) ON DELETE CASCADE,
    voluntario_id UUID NOT NULL REFERENCES usuario (id),
    data_coleta   TIMESTAMP,
    data_entrega  TIMESTAMP,
    status        VARCHAR(20) NOT NULL
                  CHECK (status IN ('PENDENTE', 'EM_ANDAMENTO', 'ENTREGUE'))
);

CREATE INDEX idx_retirada_voluntario ON retirada (voluntario_id);
CREATE INDEX idx_retirada_status ON retirada (status);

-- ---------------------------------------------------------------------
-- INDICADOR_IMPACTO  (RF06 / RN05)
--   oferta_id -> OfertaDoacao (1) : (1) IndicadorImpacto ["gera"]
-- ---------------------------------------------------------------------
CREATE TABLE indicador_impacto (
    id             UUID PRIMARY KEY,
    oferta_id      UUID NOT NULL UNIQUE REFERENCES oferta_doacao (id) ON DELETE CASCADE,
    peso_total_kg  NUMERIC(10, 2) NOT NULL,
    co2_evitado_kg NUMERIC(10, 2) NOT NULL,
    data_calculo   TIMESTAMP NOT NULL
);

-- ---------------------------------------------------------------------
-- VIEW de apoio ao RF06 (histórico consolidado)
-- ---------------------------------------------------------------------
CREATE VIEW vw_historico_doacoes AS
SELECT o.id            AS oferta_id,
       o.alimento      AS alimento,
       o.peso          AS peso,
       d.nome          AS doador,
       r.nome          AS receptor,
       v.nome          AS voluntario,
       t.data_entrega  AS data_entrega
FROM retirada t
JOIN reserva rs        ON rs.id = t.reserva_id
JOIN oferta_doacao o   ON o.id = rs.oferta_id
JOIN usuario d         ON d.id = o.doador_id
JOIN usuario r         ON r.id = rs.receptor_id
JOIN usuario v         ON v.id = t.voluntario_id
WHERE t.status = 'ENTREGUE';
