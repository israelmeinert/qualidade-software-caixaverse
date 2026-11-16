# caixaverse-rewards

Laboratório de qualidade para uma API MVC em Java 21 e Quarkus. Colaboradores recebem pontos por entregas e resgatam prêmios do catálogo.

**Regra corrigida nesta fase.** Exatamente 10 entregas geram 150 pontos. A baseline defeituosa continua na `main`.

## Executar localmente

A partir da raiz, use JDK 21, Git/Bash e Docker com Compose. O wrapper baixa Maven 3.9.11.

```bash
docker compose up -d --wait postgres
./scripts/dev-keys.sh
./mvnw quarkus:dev
```

Em outro terminal:

```bash
curl -fsS http://localhost:8080/q/health/ready
curl -fsS http://localhost:8080/catalog
ADMIN=$(./scripts/dev-token.sh aula admin)
curl -fsS -X PUT http://localhost:8080/admin/employees/ana/credits \
  -H "Authorization: Bearer $ADMIN" -H 'Content-Type: application/json' \
  -d '{"competence":"2026-11","deliveries":10}'
```

`./mvnw -B clean verify` executa os testes puros e os testes da API com PostgreSQL. `./mvnw -B test` executa somente os testes puros. O catálogo, as carteiras e os pedidos usam DTOs JSON. Consulte `/q/openapi`.

### Banco local e configuração

O Compose usa o volume `rewards-db`, com os bancos `rewards` e `rewards_test`. `.env.example` lista `POSTGRES_DB` (nome do banco de desenvolvimento), `POSTGRES_USER`, `POSTGRES_PASSWORD` (credenciais locais descartáveis) e `POSTGRES_HOST_PORT` (porta publicada em loopback). Ao personalizar os valores, exporte também os correspondentes `REWARDS_DEV_*` e `REWARDS_TEST_*` de `application.properties`. Mantenha nomes distintos para o banco de aula e o de testes.

Para remover o volume e apagar **os dois bancos locais da aula**, execute deliberadamente `docker compose down -v`. Use `docker compose down` para só desligar o banco.

### Rotas

| Método e rota | Acesso |
|---|---|
| GET `/catalog`, `/catalog/{id}` | público |
| POST `/admin/products`, PUT `/admin/products/{id}` | JWT com grupo `admin` |
| PUT `/admin/employees/{id}/credits` | JWT com grupo `admin` |
| GET `/me/wallet`, `/me/ledger` | JWT com grupo `employee` |
| POST `/me/redemptions` | JWT com grupo `employee` e `Idempotency-Key` |
| GET `/me/redemptions/{id}` | somente o dono, identificado pelo `sub` |
| GET `/q/health/live`, `/q/health/ready` | público |

A matriz correta de negócio para −1, 0, 1, 9, 10, 11, 44, 45, 46 e 60 é, respectivamente, exceção, 0, 10, 90, 150, 160, 490, 500, 500 e 500. Uma solicitação de resgate envia `productId`, `quantity` e, quando permitido, `personalization`.
