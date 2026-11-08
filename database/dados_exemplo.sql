-- =====================================================================
-- FoodLink - Dados de exemplo (opcional)
-- Senha de todos os usuários de exemplo: 1234
-- Execução: psql -U postgres -d foodlink -f database/dados_exemplo.sql
-- =====================================================================

INSERT INTO usuario (id, tipo, nome, email, documento, senha_hash, data_cadastro, limite_reservas_ativas)
VALUES
 ('11111111-1111-1111-1111-111111111111', 'DOADOR', 'Padaria Central', 'padaria@foodlink.com',
  '12.345.678/0001-99', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', CURRENT_TIMESTAMP, NULL),
 ('22222222-2222-2222-2222-222222222222', 'RECEPTOR', 'ONG Mesa Cheia', 'ong@foodlink.com',
  '98.765.432/0001-11', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', CURRENT_TIMESTAMP, 3),
 ('33333333-3333-3333-3333-333333333333', 'VOLUNTARIO', 'Ana Souza', 'ana@foodlink.com',
  '111.222.333-44', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', CURRENT_TIMESTAMP, NULL);

INSERT INTO endereco (id, rua, cidade, cep, latitude, longitude)
VALUES
 ('44444444-4444-4444-4444-444444444444', 'Rua das Flores, 100', 'Cianorte', '87200-000', -23.6636, -52.6053),
 ('55555555-5555-5555-5555-555555555555', 'Av. Brasil, 1500', 'Cianorte', '87200-100', -23.6712, -52.6120);

INSERT INTO oferta_doacao (id, doador_id, endereco_id, alimento, quantidade, peso, data_validade, observacoes, status)
VALUES
 ('66666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111',
  '44444444-4444-4444-4444-444444444444', 'Pães e massas', 20, 8.5,
  CURRENT_TIMESTAMP + INTERVAL '2' DAY, 'Retirar até as 18h', 'DISPONIVEL'),
 ('77777777-7777-7777-7777-777777777777', '11111111-1111-1111-1111-111111111111',
  '55555555-5555-5555-5555-555555555555', 'Frutas variadas', 15, 12.0,
  CURRENT_TIMESTAMP + INTERVAL '3' DAY, 'Caixas fechadas', 'DISPONIVEL');
