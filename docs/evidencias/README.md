# Evidências de execução

Execuções em **15/11/2026**, horário de São Paulo, com Temurin 21.0.13+9, Maven Wrapper 3.9.11, Quarkus 3.40.1.2 e PostgreSQL 17.12. Os números vêm da aplicação completa ou do recorte PIT indicado. O material de entrada em `conteudo/` fica fora do Git.

| Branch e commit testado | Comando | Saída | Resultado |
| --- | --- | ---: | --- |
| `main` · `a3ea11a` | `./mvnw -B clean verify` | 0 | 9 testes unitários fracos e 3 de integração passam |
| `01-sonar-ci` · `6900fba` | `./mvnw -B -Pcoverage clean verify` | 0 | 9 + 3 passam. Policy 7/7 linhas e 4/4 ramos. Projeto 183/230 linhas (79,6%) e 18/54 ramos |
| `02-mutacao-diagnostico` · `822748f` | `./mvnw -B -Pmutation clean test-compile org.pitest:pitest-maven:1.30.2:mutationCoverage` | 0 | 7 mutantes, 2 mortos e 5 sobreviventes (29%). 7/7 linhas no escopo PIT |
| `02-mutacao-diagnostico` · `822748f` | Comando PIT com `-Dpit.mutationThreshold=80` | 1 | Bloqueio real por mutation score de 29% abaixo de 80% |
| `03-testes-fortes` · `26fe746` | `./mvnw -B clean test` | 1 | 11 testes, uma falha. Em 10 entregas esperava 150 e recebeu 100. PIT fica para a fase corrigida |
| `04-correcao-qualidade` · `79305a7` | `./mvnw -B -Pcoverage clean verify` | 0 | 11 testes unitários e 8 de integração passam |
| `04-correcao-qualidade` · `79305a7` | `./mvnw -B -Pmutation test-compile org.pitest:pitest-maven:1.30.2:mutationCoverage -Dpit.mutationThreshold=80` | 0 | 7/7 mutantes mortos, score 100% |
| `final` · `743b761` | `./mvnw -B -Pcoverage clean verify` | 0 | 19 testes passam. Policy 7/7 linhas e 4/4 ramos. Projeto 219/234 linhas (93,6%) e 34/54 ramos |
| `final` · `743b761` | Comando PIT com gate de 80% e `python3 scripts/check-mutations.py target/pit-reports/mutations.xml` | 0 | 7/7 mortos, 100%, sem `NO_COVERAGE`, timeout ou erro. A fase de diagnóstico também teve zero desses estados |
| `final` · `743b761` | Scanner Maven com `sonar.qualitygate.wait=true`, conforme o [passo a passo](../sonarqube-passo-a-passo.md) | 0 | Community Build local importou um XML do mesmo SHA e aprovou a análise |
| `final` · `a379414` | Mesmo scanner com Sonar way e gate temporário `coverage < 100` | 0 / 1 | Sonar way aprovou e o gate experimental reprovou os 87,8% de coverage Sonar. Em seguida o Sonar way voltou a passar |

Os pontos de código testados em `final` têm tags `aula-final-validada` (`a379414`) e `aula-final-runtime` (`743b761`). A consolidação dos registros acima é documental.

## Relatórios e leitura

- Surefire, Failsafe e JaCoCo saem em `target/surefire-reports/`, `target/failsafe-reports/` e `target/jacoco-report/`. Abra o HTML ou consulte `jacoco.xml`. O checkout validado arquiva cópias locais, ignoradas pelo Git, em `.local/evidencias/<branch>/`.
- PIT gera `target/pit-reports/index.html` e `mutations.xml`. Um sobrevivente real foi `ConditionalsBoundaryMutator` na linha **11** de `br.com.caixaverse.rewards.model.policy.MonthlyCreditPolicy`, com descrição `changed conditional boundary`. O teste fraco passa tanto para 100 quanto para 150 retornados em 10 entregas. O teste forte da versão corrigida mata a alteração inversa. Também sobreviveram multiplicação por divisão (linha 10), soma por subtração (12) e retorno substituído por zero (14).
- SonarQube Community Build 26.11.0.134175 local, MQR Mode. O Sonar way declarou zero novas issues, coverage ≥80%, duplicação ≤3% e hotspots novos 100% revisados. A análise repetida do mesmo código teve zero novas issues e as condições sem linhas novas aplicáveis ficaram ausentes do resultado. Overall do Sonar observado no ensaio: line coverage 93,6%, coverage 87,8%, duplicação 0,0%, zero issues. No ensaio foi usado o perfil Java `Sonar way core` copiado com S1541 no limite 10 e a primeira análise como referência.
- O início rápido `./mvnw -B quarkus:dev -Dquarkus.http.port=18080` respondeu ao readiness, mostrou o catálogo semeado e aceitou um crédito autenticado de 150 pontos.
- `docker build -t caixaverse-rewards:qa -f Dockerfile .` e Compose de entrega passaram localmente. O smoke com PostgreSQL dedicado verificou readiness, ausência de dados de aula em `prod`, crédito autenticado de 150 pontos e conflito 409 sem o dado de entrada nos logs. A rede de QA usou uma sub-rede local livre porque os pools automáticos do daemon estavam esgotados. O override desse ensaio é local.
- `actionlint` 1.7.12, `bash -n` e `docker compose config --quiet` passaram. O agregador aceitou `success` (saída 0) e recusou `failure`, `cancelled` e `skipped` (saída 1). Fixtures locais de promoção aceitaram o manifesto esperado e rejeitaram workflow, branch, ancestralidade, tentativa, SHA de artefato, staging pulado e imagem de outro repositório. O teste de reversão simulou falha de smoke e restauração do digest anterior.
- GitHub Actions, registry GHCR, acesso SSH a staging/produção, regras de proteção e credenciais remotas são pendências externas. Os ensaios locais da lógica de promoção e reversão usam fixtures e não representam uso dessas integrações reais.

## Repetir a prova do agregador

```bash
for result in success failure cancelled skipped; do
  REQUIRED_RESULTS=$(printf '{"tests":{"result":"success"},"sonar":{"result":"%s"}}' "$result") \
    bash scripts/require-success.sh
  printf '%s saiu com %d\n' "$result" "$?"
done
```

Veja o [checklist dos critérios de aceitação](aceitacao.md).
