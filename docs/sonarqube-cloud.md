# SonarQube Cloud

A branch `final` envia a cobertura Maven e bloqueia a entrega quando o Quality Gate do SonarQube Cloud reprova. Os pushes de `final` disparam a análise. Nos PRs com destino a `final`, os checks do repositório continuam executando os testes e o PIT.

## Vincular o projeto

Importe somente o repositório `israelmeinert/qualidade-software-caixaverse` na organização do SonarQube Cloud e confira a visibilidade privada. Configure `final` como a branch principal no painel do Sonar. Verifique o plano da organização se o Cloud tratar `final` como branch adicional. Se a análise automática estiver ativa, desligue-a no projeto antes de usar a análise via GitHub Actions. A análise do CI usa o XML JaCoCo gerado a partir do mesmo commit.

No GitHub, cadastre o secret `SONAR_CLOUD_TOKEN` com permissão de executar análise. Cadastre também as variáveis `SONAR_CLOUD_ORGANIZATION` e `SONAR_CLOUD_PROJECT_KEY` com as chaves que o SonarQube Cloud mostra. Use `SONAR_CLOUD_HOST_URL` para explicitar a instância regional quando necessário. O workflow usa `https://sonarcloud.io` por padrão.

## Analisar pelo Maven

Disponibilize o token em `SONAR_TOKEN` por um meio que o mantenha fora do Git e do histórico do terminal. Exporte também `SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY` com as chaves do Cloud. Para a análise com cobertura, suba o PostgreSQL e execute:

```bash
docker compose up -d --wait postgres
./mvnw -B -Pcoverage,sonar-cloud clean verify sonar:sonar
```

No MCP oficial, defina `SONARQUBE_URL=https://sonarcloud.io`, `SONARQUBE_ORG`, `SONARQUBE_PROJECT_KEY` e um token de usuário em `SONARQUBE_TOKEN`. Guarde as credenciais na configuração local do MCP. O scanner Maven usa o token de análise e o MCP usa o token de consulta ao Cloud.

[Conectar o GitHub ao SonarQube Cloud](https://docs.sonarsource.com/sonarqube-cloud/getting-started/github) · [Planos e análise de branches](https://docs.sonarsource.com/sonarqube-cloud/administering-sonarcloud/managing-subscription/subscription-plans) · [MCP oficial](https://github.com/SonarSource/sonarqube-mcp-server).
