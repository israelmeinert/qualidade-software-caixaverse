# Diagnóstico

| Sintoma | Verificação e correção |
| --- | --- |
| `invalid target release: 21` | Use JDK completo, confira `java -version`, `javac -version` e `./mvnw -version`. Preserve `release=21`. |
| Wrapper falha no download | Confira acesso a Maven Central, proxy e checksum. Preserve a distribuição 3.9.11. |
| Teste HTTP não aparece em `test` | Surefire seleciona `*Test`. Failsafe seleciona `*IT` em integration-test e verifica o resultado em `verify`. |
| Falta `jacoco.xml` | Rode `./mvnw -B -Pcoverage clean verify` com PostgreSQL pronto. A extensão gera o relatório combinado no encerramento do teste Quarkus. Confira `target/jacoco-report`. |
| PIT `NO_COVERAGE` ou teste original falha | Confirme o pacote, glob, Jupiter/Platform 6.1.3, adaptador 1.2.3 e rodagem verde. Na fase 03 a falha original em 10 é proposital. |
| PostgreSQL inacessível | `docker compose ps`, `docker compose logs postgres`. Confira loopback, porta 55432 e o banco separado `rewards_test`. O init cria bancos somente no primeiro boot do volume. A limpeza explícita do volume apaga os dados locais da aula. |
| Erro JWT em dev | Execute `./scripts/dev-keys.sh` desde o projeto, confirme o relógio e gere token novo. Em produção, use chave pública e emissor do ambiente. |
| Sonar sem memória ou ES não inicia | Leia `docker compose -f compose.sonar.yaml logs sonar`, confira memória e limites do host descritos em [SonarQube](sonarqube-passo-a-passo.md). |
| Scanner sem cobertura ou sem gate | Confirme XML não vazio, SHA correspondente, token, URL alcançável do runner e chave do projeto. Abra Background Tasks e verifique o resultado assíncrono e a configuração de pequenas mudanças. |
| Secret ou variable faltante | Leia a falha do job e cadastre a configuração do repositório ou Environment correto. Em Community, PR roda testes e PIT sem segredo. Em push de `final`, Sonar ausente bloqueia a entrega. |
| Artefato de release ausente ou expirado | Confirme run/tentativa completa, staging habilitado, sucesso de todos os jobs e retenção. Reexecute todos os jobs para gerar nova evidência. |
