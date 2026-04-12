-- Migração dos campos monetários de FLOAT para NUMERIC (BigDecimal-safe)
-- Escala: 15,2 para valores em R$ | 5,4 para fatores de margem (ex: 0.1000)

ALTER TABLE tipo_grao
    ALTER COLUMN preco_por_tonelada TYPE NUMERIC(15, 2) USING preco_por_tonelada::NUMERIC(15, 2);

ALTER TABLE pesagem
    ALTER COLUMN custo_carga      TYPE NUMERIC(15, 2) USING custo_carga::NUMERIC(15, 2),
    ALTER COLUMN margem_aplicada  TYPE NUMERIC(5, 4)  USING margem_aplicada::NUMERIC(5, 4);
