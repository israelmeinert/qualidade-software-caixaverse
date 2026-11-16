# Cobertura em testes JVM

`./mvnw -B -Pcoverage clean verify` grava o HTML e XML em `target/jacoco-report/`. Abra o HTML e compare o total do projeto à classe `MonthlyCreditPolicy`.

Surefire roda `*Test` na fase `test`. Failsafe roda `*IT` por `integration-test` e valida a execução em `verify`. O agente Maven grava as classes da JVM normal em `target/jacoco-quarkus.exec` com append e exclui `*QuarkusClassLoader`. Durante `@QuarkusTest`, a extensão `quarkus-jacoco` instrumenta as classes carregadas pelo Quarkus, usa o mesmo arquivo com reutilização e gera os relatórios combinados. Cada execução de `clean` descarta os dados anteriores. Não combine os profiles `coverage` e `mutation`.

Esses relatórios medem processos JVM dessa suíte. Testes de executável nativo ou de processos externos exigem instrumentação/estratégia própria. O Sonar recebe o XML do mesmo SHA, junto das classes compiladas. Ele calcula suas métricas de cobertura a partir dos dados importados e das linhas analisáveis. Verifique o escopo quando comparar o valor do painel com o de linhas JaCoCo.

[Guia Quarkus](https://quarkus.io/guides/tests-with-coverage) · [Contadores JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/counters.html) · [Cobertura Java no Sonar](https://docs.sonarsource.com/sonarqube-community-build/analyzing-source-code/test-coverage/java-test-coverage).
