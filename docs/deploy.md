# Entrega para uma VM Linux

O fluxo está **configurado no código, ainda não validado remotamente**. A branch `final` é a candidata e a branch padrão a configurar no GitHub. A `main` preserva o laboratório inicial. A publicação em GHCR só ocorre em push de `final`, após testes, PIT com limiar 80 e Quality Gate Sonar do mesmo SHA.

## Preparar staging e produção

1. Prepare duas VMs Linux amd64, cada uma com Docker Engine/Compose, Bash, curl, flock e acesso ao GHCR. Use conta dedicada, Docker rootless ou isolamento equivalente. A conta deve controlar exclusivamente os recursos de implantação da VM. Publique a API por proxy TLS conforme a rede do ambiente. O Compose expõe a porta HTTP em loopback e não publica a porta do banco.
2. Instale `compose.deploy.yaml` de uma revisão aprovada no diretório de cada ambiente, por exemplo `/opt/caixaverse-rewards`. Instale ali `jwt-public.pem` do emissor do ambiente, legível pelo UID 10001 do container. Configure o emissor para assinar JWT RS256 com os papéis do contrato. Nunca copie a chave privada para a VM da aplicação.
3. Crie `.env` de cada VM com modo 600. Defina `REWARDS_DB_USER`, `REWARDS_DB_PASSWORD`, `REWARDS_JWT_ISSUER` e, se necessária, `APP_PORT` (8080 por padrão). O Compose mantém PostgreSQL e o volume da VM. Crie backup e teste restauração antes de promover mudanças de schema.
4. Para imagem GHCR privada, faça login com credencial de serviço de leitura de packages e autorização ao pacote. Use `docker login ghcr.io --password-stdin` e um credential store apropriado na conta de deploy. Faça a autenticação em cada VM e rotacione o token. O `GITHUB_TOKEN` de publicação existe no job temporário, com `packages: write` apenas nesse job.
5. Configure uma chave SSH exclusiva por ambiente e restrinja a conta de deploy. Use entrada de `known_hosts` obtida por canal confiável e verifique o fingerprint antes de armazená-la. O script exige `StrictHostKeyChecking=yes`. Permita rede SSH só a partir de runners autorizados.
6. Crie GitHub Environments `staging` e `production`. Cadastre em cada um as variables `DEPLOY_HOST`, `DEPLOY_PORT` (opcional, 22), `DEPLOY_USER`, `DEPLOY_DIR` e `DEPLOY_SMOKE_URL` (exemplo `http://127.0.0.1:8080/q/health/ready`, resolvida pela VM). Cadastre `DEPLOY_SSH_KEY` e `DEPLOY_KNOWN_HOSTS` como secrets do Environment. Defina a variable de repositório `STAGING_ENABLED=true` depois de preparar a VM de staging.
7. Para `production`, em Deployment branches and tags selecione uma regra de branch que permita **somente `final`**. Configure required reviewers e impeça autoaprovação onde o plano permitir. Sem o recurso, exija autorização externa independente registrada no PR ou chamado, limite quem pode operar o dispatch e anexar o ID verificado. Clicar Run workflow é acionamento manual. A aprovação independente exige outro agente autorizado.
8. Proteja `final` contra pushes e force-pushes diretos no fluxo normal, exija revisão em alterações de workflows/scripts e use owners reais. Configure Sonar e checks como em [SonarQube](sonarqube-passo-a-passo.md). Use as capacidades do plano privado disponível.

## Publicação e promoção

O job `publish-image` empacota o fast-jar, cria uma imagem JRE para o SHA, publica a tag por SHA e lê o digest do Buildx. O registry recebe a proveniência BuildKit em modo mínimo. O job `deploy-staging` implanta exatamente `ghcr.io/owner/repo@sha256:…`, espera readiness via Compose, faz smoke via HTTP e só então envia `release.json` como artefato `release-RUN-ATTEMPT`.

No GitHub, confirme a execução de **Qualidade** e o success de staging. Em **Promover produção**, selecione a referência `final` e informe o ID numérico da execução de entrega. O job inicial rejeita qualquer outra ref. Ele verifica pela API do GitHub o workflow, repositório, evento, branch, SHA, tentativa e resultado, além dos seis jobs exigidos na mesma tentativa e dos metadados originais do artefato. Verifica que o SHA é ancestral da revisão de `final` do dispatch. O manifesto tem de pertencer à mesma execução e identificar imagem GHCR deste repositório. O job protegido de produção recebe somente o digest resolvido. Não há rebuild.

O manifesto expira após 30 dias. Se estiver ausente ou jobs pertencerem a tentativas diferentes, reexecute **todos os jobs** da entrega e repita a verificação em staging. Reexecutar só jobs falhos pode deixar evidências partidas entre tentativas. Se staging estiver desabilitado, nenhuma execução serve como candidata a produção. Use a lista dos Deployments e compare SHA/digest nas três etapas.

## Verificação e reversão

A concorrência do GitHub serializa jobs de cada ambiente e o script usa lock também na VM. A imagem é puxada antes da atualização. O estado `.current-image` é gravado por renomeação após healthcheck e smoke. `.previous-image` guarda o digest que estava em execução antes da troca. Se o smoke falhar, o script tenta restaurar a imagem atual anterior e termina com erro. Na primeira implantação ruim, para a aplicação. O banco e o volume ficam presentes.

Para reversão manual por incidente, após autorização e revisão do schema execute:

```bash
cd /opt/caixaverse-rewards
previous=$(cat .previous-image)
bash /caminho/verificado/update.sh "$PWD" "$previous" http://127.0.0.1:8080/q/health/ready
```

O rollback da aplicação não desfaz migrations. Use evolução de schema compatível com a imagem anterior e plano separado para restauração dos dados. Verifique também o log da aplicação, 5xx e o endpoint operacional após trocar a imagem.

Referências: [Environments](https://docs.github.com/en/actions/how-tos/deploy/configure-and-manage-deployments/manage-environments), [GHCR](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry), [BuildKit provenance](https://docs.docker.com/build/metadata/attestations/slsa-provenance/).
