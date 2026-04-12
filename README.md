# grain-transport-api

API REST para gestão de transporte de grãos com estabilização de leituras de balança IoT (ESP32).

**Deploy:** https://grain-transport-api.onrender.com  
**Swagger UI:** https://grain-transport-api.onrender.com/swagger-ui.html  
**API Docs (OpenAPI):** https://grain-transport-api.onrender.com/api-docs

---

## Stack

| Camada | Tecnologia |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.x |
| Banco de dados | PostgreSQL 16 |
| Migrations | Flyway |
| Build | Maven (wrapper) |
| Container | Docker (multi-stage) |
| Deploy | Render |

---

## Arquitetura

```
┌────────────────────────────────────────────────────────────────────┐
│                          Dispositivos IoT                          │
│                    ESP32 (balança de caminhão)                     │
│          POST /api/ingest  { id, plate, weight }                   │
│          Header: X-Api-Key: <chave da balança>                     │
└─────────────────────────────┬──────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────────┐
│                        Spring Boot API                             │
│                                                                    │
│  ┌─────────────┐    ┌──────────────────┐    ┌──────────────────┐   │
│  │ ApiKeyFilter│───▶│IngestionController│───▶│IngestionService │   │
│  │ (autenticaç.│    │  idempotência    │    │  buffer em mem.  │   │
│  │  por balança│    │  202 / 200       │    │  ConcurrentMap   │   │
│  └─────────────┘    └──────────────────┘    └────────┬─────────┘   │
│                                                      │             │
│                              ┌───────────────────────┘             │
│                              │ @Scheduled (2s)                     │
│                              ▼                                     │
│                    ┌──────────────────┐                            │
│                    │StabilizationSvc  │                            │
│                    │ Sliding Window   │                            │
│                    │ Variance Check   │──── persiste Pesagem       │
│                    └──────────────────┘                            │
│                                                                    │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │                    CRUD Controllers                        │    │
│  │  Filial · Caminhao · TipoGrao · Balanca · Transacao        │    │
│  └────────────────────────────────────────────────────────────┘    │
│                                                                    │
│  ┌────────────────────────────────────────────────────────────┐    │ 
│  │                    Stats Controllers                       │    │
│  │  GET /api/stats/pesagens · /custos · /lucros               │    │
│  │  Filtros: filialId, caminhaoId, tipoGraoId, inicio, fim    │    │
│  │  Agrupamento: FILIAL | CAMINHAO | GRAO | PERIODO           │    │
│  └────────────────────────────────────────────────────────────┘    │
└────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │   PostgreSQL     │
                    │ filial           │
                    │ balanca          │
                    │ caminhao         │
                    │ tipo_grao        │
                    │ transacao_transporte │
                    │ pesagem          │
                    └──────────────────┘
```

### Modelo de dados resumido

```
filial ──< transacao_transporte >── caminhao
                │                       
                └── tipo_grao           
                │                       
                └──< pesagem >── balanca
```

---

## Fluxo de estabilização

O maior diferencial técnico da API é o pipeline de estabilização das leituras do ESP32:

```
ESP32 envia leitura
       │
       ▼
ApiKeyFilter valida X-Api-Key
       │
       ▼
IngestionController gera chave de idempotência
  chave = balancaId + "_" + plate + "_" + timestamp
  ├── duplicata? → HTTP 200 { status: "duplicata" }  ← retentativa segura
  └── nova?      → HTTP 202 { status: "accepted" }
                      │
                      ▼
             IngestionService
             adiciona leitura ao buffer (Deque por placa)
                      │
                      ▼
     @Scheduled a cada 2s: StabilizationService.processarBuffer()
             ├── janela < windowSize (padrão 5)? → aguarda mais leituras
             ├── max(pesos) - min(pesos) > threshold (padrão 3kg)? → descarta, loga warning
             └── estável? → calcula peso médio, persiste Pesagem, limpa buffer
```

### Decisões de estabilização

**Por que Sliding Window?**  
Balanças industriais vibram com o movimento do caminhão. Aguardar N leituras consecutivas dentro de um threshold de variação garante que apenas medições estabilizadas (caminhão parado, molas amortecidas) sejam persistidas, eliminando ruído de medição.

**Por que Variance Threshold e não média simples?**  
Uma média suaviza o erro mas não detecta instabilidade. Com `max - min ≤ 3kg` temos uma garantia explícita de que as leituras estão convergindo — se houver qualquer perturbação durante a janela, o sistema aguarda e tenta na próxima rodada.

**Por que idempotência no controller e não no banco?**  
O ESP32 tem lógica de retry por TCP: em caso de timeout ele reenvia a mesma leitura. A idempotência em memória com janela de 30s descarta esses duplicatas antes de qualquer processamento, evitando que o buffer seja poluído com a mesma leitura N vezes. Custo: reenvios após reinício da JVM podem passar — aceitável para um MVP.

**Por que margem dinâmica baseada em estoque?**  
Simula comportamento real de precificação: quando o estoque está alto (≥ 1000t) a margem é mínima (5%) para incentivar escoamento; com estoque baixo (≤ 100t) a margem sobe para 20%. A interpolação linear entre os dois extremos foi escolhida pela previsibilidade e facilidade de ajuste.

---
## Como executar localmente

### Pré-requisitos

- Java 21+
- Docker compose
- maven/Intellij

### 1. Clonar repositório

```bash
git clone https://github.com/Rodrigo-Sartori/grain-transport-api.git
```

### 2. Entrar no diretório do projeto


```bash
cd .\grain-transport-api\
```

### 3. Rodar via Docker (build local)

```bash
docker compose up --build
```
O mesmo terminal irá executar o banco de dados e a Aplicação; A aplicação sobe em `http://localhost:8080`. O Flyway roda as migrations automaticamente.

### 4. Rodar os testes
Opcionalmente é possivel rodar os testes via maven na linha de comando ou executar via Intellij, tanto via menu maven ou via java com o Project Run

#### 4.1. Rodar os testes via bash
```bash
./mvnw test
```

#### 4.2. Rodar os testes via Intellij
Maven -> Test -> Run Maven Build

Selecionar caminho na raiz do projeto a partir da pasta test e usar o atalho de tecla CTRL+SHIFT+F10 



---

## Variáveis de ambiente

| Variável | Padrão (dev local) | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/grain_transport` | URL JDBC do banco |
| `DB_USER` | `grain_user` | Usuário do banco |
| `DB_PASSWORD` | `grain_pass` | Senha do banco |
| `PORT` | `8080` | Porta HTTP (Render injeta automaticamente) |

---

## Sugestões de evolução

### Curto prazo (próximas iterações)

**1. Idempotência persistida no banco**  
A atual implementação em `ConcurrentHashMap` perde o estado no restart da JVM. Uma opção mais robusta seria usar um banco de dados de Cache como Redis utilizando `SETNX` + `EXPIRE`.

**2. Websocket ou SSE para monitoramento ao vivo**  
Atualmente não há forma de acompanhar em tempo real o estado dos buffers. Um endpoint `GET /api/ingest/status` via Server-Sent Events mostraria quais placas estão com leituras enfileiradas, quantas leituras acumuladas e o status de estabilização.

**3. Alertas de estoque crítico**  
O `calcularMargem` já detecta estoque baixo. Falta a notificação: ao persistir uma pesagem com estoque ≤ 100t, disparar um evento (Spring `ApplicationEvent` → listener → e-mail/webhook) para avisar o gestor.

**4. Autenticação JWT para os endpoints de gestão**  
Hoje apenas a ingestão exige `X-Api-Key`. Os endpoints CRUD e de stats são públicos. O próximo passo óbvio é adicionar Spring Security com JWT para separar o acesso do firmware (API Key) do acesso humano (JWT com roles `ADMIN` / `OPERADOR`).

### Médio prazo (escala e resiliência)

**5. Substituir buffer em memória por fila de mensagens**  
O `ConcurrentHashMap` no `IngestionService` é um ponto único de falha: em múltiplas instâncias da API, cada instância tem seu próprio buffer e a mesma placa pode ser dividida entre pods. A solução é mover o buffer para **Kafka** ou **RabbitMQ**, com o `StabilizationService` consumindo de um tópico/fila particionado por `plate`. Isso abre caminho para escalar horizontalmente sem risco.

**6. Retry + Dead Letter Queue**  
Com filas no lugar, leituras que falham no processamento (ex: transação não encontrada) vão para uma DLQ em vez de serem silenciosamente descartadas. Permite análise post-mortem e reprocessamento manual.

**7. Cache na camada de stats**  
Os endpoints `/api/stats/*` fazem full-scan via `findByFiltro` a cada chamada. Com volume real de pesagens, isso vai pesar. A primeira otimização é `@Cacheable` com Spring Cache + Redis, com invalidação quando novas pesagens são persistidas. Segunda otimização: pré-calcular agregações em tabelas de summary atualizadas por job agendado.

**8. Observabilidade**  
Adicionar Micrometer + Prometheus + Grafana dashboard. Métricas prioritárias:
- `pesagens.stabilization.success` / `pesagens.stabilization.failed` (counter)
- `pesagens.buffer.size` por placa (gauge)
- `ingestion.duplicata.count` (counter)
- Latência dos endpoints de stats (histogram)

### Longo prazo (produto)

**9. ML para calibração automática do threshold**  
O `variance-threshold` é configurado manualmente (padrão 3kg). Com histórico de leituras, um modelo simples (regressão linear ou média móvel exponencial) poderia ajustar o threshold por balança individualmente com base nas características mecânicas de cada equipamento.

**10. Multi-tenant por filial**  
Cada filial poderia ter sua própria configuração de `window-size`, `variance-threshold` e até regras de margem. Implementável com uma tabela `configuracao_filial` e sobrescrita das configurações globais do `application.yml` por lookup no início do processamento.

---

## Estrutura do projeto

```
src/
├── main/java/.../
│   ├── config/          # GlobalExceptionHandler, SchedulerConfig, SwaggerConfig
│   ├── controller/      # IngestionController, StatsController, CRUDs
│   ├── domain/          # Entidades JPA + enum GroupBy
│   ├── dto/             # Request/Response DTOs + Stats DTOs (Records)
│   ├── filter/          # ApiKeyFilter
│   ├── mapper/          # Mappers estáticos (sem MapStruct)
│   ├── repository/      # Spring Data JPA repositories
│   └── service/         # IngestionService, StabilizationService, StatsService, CRUDs
└── main/resources/
    ├── application.yml
    └── db/migration/    # V1 create tables, V2 seed, V3 BigDecimal, V4 idempotency refactor
```
