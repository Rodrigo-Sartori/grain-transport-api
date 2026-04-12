-- Remove idempotency_key da pesagem — idempotência agora é responsabilidade da camada de ingestão
ALTER TABLE pesagem DROP CONSTRAINT IF EXISTS uk_pesagem_idempotency_key;
ALTER TABLE pesagem DROP COLUMN IF EXISTS idempotency_key;

-- Adiciona criado_em: timestamp de criação do registro, gerenciado pelo Hibernate @CreationTimestamp
ALTER TABLE pesagem ADD COLUMN criado_em TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE pesagem ALTER COLUMN criado_em DROP DEFAULT;
