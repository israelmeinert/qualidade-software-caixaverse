# Versões e fontes primárias

Consulta em 15/11/2026. O Maven usa o BOM do Quarkus para evitar versões divergentes na suíte.

| Componente | Versão e fonte |
| --- | --- |
| JDK da execução | Temurin 21.0.13+9, compilação `release=21`. [Release do JDK](https://github.com/adoptium/temurin21-binaries/releases/tag/jdk-21.0.13%2B9) |
| Maven Wrapper | Wrapper only-script 3.3.4, Maven 3.9.11 com SHA-256 da distribuição. [Wrapper Apache](https://maven.apache.org/wrapper/) · [Distribuição](https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/) |
| Quarkus | BOM e plugin 3.40.1.2. [BOM publicado](https://repo.maven.apache.org/maven2/io/quarkus/platform/quarkus-bom/3.40.1.2/quarkus-bom-3.40.1.2.pom) · [Releases](https://quarkus.io/releases/) |
| JUnit | Jupiter 6.1.3 e Platform 6.1.3, gerenciados pelo BOM. [JUnit](https://docs.junit.org/) |
| Testes Maven | Surefire/Failsafe 3.5.4. [Failsafe](https://maven.apache.org/surefire/maven-failsafe-plugin/) |
| JaCoCo | 0.8.15. [Guia Quarkus](https://quarkus.io/guides/tests-with-coverage) · [Contadores JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/counters.html) |
| PIT | Maven 1.30.2 e `pitest-junit5-plugin` 1.2.3, mesmo com Jupiter 6. [Metadados Maven](https://repo.maven.apache.org/maven2/org/pitest/pitest-maven/maven-metadata.xml) · [Adaptador](https://github.com/pitest/pitest-junit5-plugin) |
| SonarScanner | Maven 5.8.0.7211. JDK 21 no job, autoaprovisionamento permitido. [Requisitos e releases](https://docs.sonarsource.com/sonarqube-community-build/analyzing-source-code/scanners/sonarscanner-for-maven) |
| SonarQube | Community Build 26.11.0.134175, MQR Mode, análise da branch principal. [Escopo](https://docs.sonarsource.com/sonarqube-community-build/analyzing-source-code/analysis-overview) · [Gate](https://docs.sonarsource.com/sonarqube-community-build/quality-standards-administration/managing-quality-gates/introduction-to-quality-gates) |
| PostgreSQL | 17.12-alpine, fixado por digest nos Composes. [Suporte de versões](https://www.postgresql.org/support/versioning/) · [Imagem](https://hub.docker.com/_/postgres) |
| Runtime JVM | Temurin 21.0.13_9-jre-jammy, digest fixado no Dockerfile. Build da aplicação usa Maven Wrapper e JDK 21 no runner. [Imagem Temurin](https://hub.docker.com/_/eclipse-temurin) |
| Checkout | v7.0.1. [3d3c42e5aac5ba805825da76410c181273ba90b1](https://github.com/actions/checkout/commit/3d3c42e5aac5ba805825da76410c181273ba90b1) |
| Setup Java | v6.3.0. [d1374f8ac74474200d16ef8ecee22c7ae7a51880](https://github.com/actions/setup-java/commit/d1374f8ac74474200d16ef8ecee22c7ae7a51880) |
| Upload Artifact | v7.0.2. [cf430e030ddbb5b0abf93d22962f4752f3646cd9](https://github.com/actions/upload-artifact/commit/cf430e030ddbb5b0abf93d22962f4752f3646cd9) |
| Download Artifact | v8.0.2. [9000827ccba6bdab643e8b6fd33ac0654aef8333](https://github.com/actions/download-artifact/commit/9000827ccba6bdab643e8b6fd33ac0654aef8333) |

Verifique a resolução local com `./mvnw -version` e `./mvnw -B -Pcoverage dependency:tree -Dincludes='org.junit.jupiter:*,org.junit.platform:*,org.jacoco:*'`. Confira os digests efetivamente usados nos arquivos de infraestrutura.

Referências de operação: [eventos e branch padrão](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#workflow_dispatch), [status checks](https://docs.github.com/en/pull-requests/how-tos/merge-and-close-pull-requests/troubleshooting-required-status-checks), [uso seguro de Actions](https://docs.github.com/en/actions/reference/security/secure-use), [environments](https://docs.github.com/en/actions/how-tos/deploy/configure-and-manage-deployments/manage-environments), [API de execuções](https://docs.github.com/en/rest/actions/workflow-runs), [API de artefatos](https://docs.github.com/en/rest/actions/artifacts).
