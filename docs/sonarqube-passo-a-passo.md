# SonarQube Community Build

A trilha usa Community Build com MQR Mode e analisa somente a branch principal, configurada como `final`. Em PR a verificação local do runner inclui testes e, a partir da fase de correção, PIT. Este produto não analisa nem decora PRs. Para uma trilha de análise de PR, verifique Developer Edition ou um plano Cloud com o recurso, habilite GitHub App com acesso ao repositório e adapte os checks. Forks normalmente ficam sem secrets, portanto não recebem aprovação Sonar quando ela for exigida.

## Ambiente e projeto

1. Para uma demonstração local, use `docker compose -f compose.sonar.yaml up -d`. Reserve pelo menos 4 GB para Sonar mais a memória da aplicação e do banco. No host Linux configure `vm.max_map_count >= 524288`, `fs.file-max >= 131072`, `nofile >= 131072` e `nproc >= 8192`. Para laboratório descartável de nó único, a variável `SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true` permite tentar o boot sem esses checks. O H2 incluído serve para esta instância local e tem volumes próprios.
2. Acesse `http://localhost:19000`, redefina a senha inicial e crie um projeto manual com chave `caixaverse-rewards`. O caminho Community do laboratório usa token de análise e vínculo manual com o projeto GitHub para rastreio do SHA, sem GitHub App ou decoração. Para o runner hospedado configure uma URL Sonar alcançável por HTTPS, ou um runner deliberadamente isolado com acesso à rede do Sonar.
3. Em administração do projeto, configure a branch principal como `final` antes da análise. Gere um **Project Analysis Token** com permissão de análise apenas nesse projeto e validade definida. Crie no GitHub o secret `SONAR_TOKEN` e as variables `SONAR_HOST_URL` e `SONAR_PROJECT_KEY`. Não passe o token por argumento Maven.
4. Execute a primeira análise em `final`. Configure New Code por **Specific analysis** com o ID da análise de baseline ou por **Previous version** com versionamento explícito. Não envie as branches didáticas à instância Community no lugar da principal.
5. Em Quality Profiles, copie o perfil Java Sonar way e associe o perfil ao projeto. Busque `java:S1541` e, se disponível, ative com limite 10 por método. Complexidade cognitiva é outra regra, `java:S3776`. Um método acima do limite passa a gerar issue pela regra do profile. O gate avalia o conjunto de condições disponíveis e pode reprovar pela issue nova. Uma nota A de manutenibilidade pode coexistir com issues.
6. Em Quality Gates, inspecione as condições do **Sonar way** efetivo. A referência é zero issues novas, cobertura de código novo ≥80%, duplicação ≤3% e revisão de hotspots novos quando exposta. Hotspots estão em processo de descontinuação nesta família de versões. Registre o que a instância expõe e qual regra substitui a revisão quando o produto migra para issues de segurança. Use métricas MQR conforme a instância, sem misturar os nomes do Standard Experience. Associe o gate ao projeto em Project Settings.
7. Confira a configuração de pequenas mudanças no gate e na instância. O padrão pode ignorar cobertura/duplicação para uma alteração com menos de 20 linhas novas. Para a aula, desative essa exceção na configuração `Ignore duplication and coverage on small changes` se disponível. Registre a configuração que de fato foi aplicada.

## Executar e interpretar

```bash
# SONAR_TOKEN e SONAR_HOST_URL já devem estar no ambiente.
./mvnw -B -Pcoverage clean verify \
  org.sonarsource.scanner.maven:sonar-maven-plugin:5.8.0.7211:sonar \
  -Dsonar.projectKey=caixaverse-rewards \
  -Dsonar.qualitygate.wait=true -Dsonar.qualitygate.timeout=300
```

Abra Background Tasks, confirme o SHA e a origem do XML, depois compare Overview, Measures e Issues. O scanner importa o JaCoCo de `target/jacoco-report/jacoco.xml`. O status do gate vem do processamento assíncrono; timeout, erro ou token ausente bloqueiam o fluxo de entrega. Um exemplo de reprovação é uma nova issue de `S1541` sob a condição zero issues novas. Registre qual regra e condição aparecem antes de dizer por que o gate reprovou.

No GitHub, publique primeiro somente quando autorizado. Defina `final` como branch padrão e destino das manutenções. Após uma execução, selecione o check de PR `quality-required` que apareceu de fato e exija revisões no ruleset de `final`. O agregador de entrega `quality-required-delivery` bloqueia publicação após merge e depende de Sonar. Se o plano privado não suportar rulesets, registre a limitação e institua revisão independente antes do merge; valide o check no SHA revisado.

[Documentação Community](https://docs.sonarsource.com/sonarqube-community-build/) · [Quality Gates](https://docs.sonarsource.com/sonarqube-community-build/quality-standards-administration/managing-quality-gates/introduction-to-quality-gates) · [Pequenas mudanças](https://docs.sonarsource.com/sonarqube-community-build/quality-standards-administration/managing-quality-gates/managing-custom-quality-gates).
