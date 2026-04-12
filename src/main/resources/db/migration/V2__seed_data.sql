-- ============================================================
-- V2: Dados iniciais para desenvolvimento e testes
-- ============================================================

-- Filiais
INSERT INTO filial (nome, cidade) VALUES
    ('Filial Sudoeste', 'Cascavel'),
    ('Filial Centro-Oeste', 'Rondonópolis'),
    ('Filial Norte', 'Sinop')
ON CONFLICT DO NOTHING;

-- Balanças (api_key usada no header X-Api-Key)
INSERT INTO balanca (codigo, api_key, filial_id) VALUES
    ('BAL-001', 'key-balanca-001-dev', 1),
    ('BAL-002', 'key-balanca-002-dev', 1),
    ('BAL-003', 'key-balanca-003-dev', 2),
    ('BAL-004', 'key-balanca-004-dev', 3)
ON CONFLICT DO NOTHING;

-- Caminhões (tara em kg)
INSERT INTO caminhao (placa, tara) VALUES
    ('ABC-1234', 8500.0),
    ('DEF-5678', 9200.0),
    ('GHI-9012', 7800.0),
    ('JKL-3456', 10500.0)
ON CONFLICT DO NOTHING;

-- Tipos de Grão
-- estoque_toneladas influencia a margem dinâmica:
--   >= 1000t → 5%  |  <= 100t → 20%  |  entre → interpolação
INSERT INTO tipo_grao (nome, preco_por_tonelada, estoque_toneladas) VALUES
    ('Soja',   2800.00, 1500.0),   -- estoque alto → margem 5%
    ('Milho',  1600.00,  450.0),   -- estoque médio → margem ~14%
    ('Trigo',  2200.00,   80.0),   -- estoque baixo → margem 20%
    ('Sorgo',  1400.00,  900.0)    -- estoque médio-alto → margem ~7%
ON CONFLICT DO NOTHING;

-- Transação de exemplo (aberta, para uso em testes de ingestão)
INSERT INTO transacao_transporte (caminhao_id, tipo_grao_id, filial_id, iniciada_em) VALUES
    (1, 1, 1, now())
ON CONFLICT DO NOTHING;
