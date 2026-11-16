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

### Percurso da aula

| Branch | Tag | Marco |
| --- | --- | --- |
| main | aula-main | MVC, banco e baseline fraca com bônus incorretamente exclusivo |
| 01-sonar-ci | aula-sonar | JaCoCo combinado, perfil/gate Sonar e revisão |
| 02-mutacao-diagnostico | aula-mutacao | PIT de diagnóstico com score mínimo zero |
| 03-testes-fortes | aula-vermelha | Oráculos independentes e falha intencional para 10 entregas |
| 04-correcao-qualidade | aula-correcao | Fronteira corrigida, concorrência/integridade verificadas e score mínimo de 80% |
| final | aula-final | CI/CD, roteiro, exercícios e evidências do laboratório |

~~~bash
git switch main
./mvnw -B clean test
git switch 01-sonar-ci
docker compose up -d --wait postgres
./mvnw -B -Pcoverage clean verify
git switch 02-mutacao-diagnostico
./mvnw -B -Pmutation clean test-compile org.pitest:pitest-maven:1.30.2:mutationCoverage
git switch 03-testes-fortes
./mvnw -B clean test # falha esperada para 10 entregas
git switch final
./scripts/demo.sh verify
./scripts/demo.sh pit
~~~

Consulte o [roteiro do professor](docs/roteiro-demo.md), [JaCoCo](docs/jacoco.md), [PIT](docs/mutacao.md), [SonarQube](docs/sonarqube-passo-a-passo.md), [evidências](docs/evidencias/README.md) e [exercícios](exercicios/README.md). Produção e staging estão configurados no código, ainda não validados remotamente. O [guia de entrega](docs/deploy.md) descreve a infraestrutura e a configuração para publicação posterior.
