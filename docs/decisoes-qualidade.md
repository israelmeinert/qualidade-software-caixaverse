# Política inicial de qualidade

| Controle | Escopo e decisão | Responsável |
| --- | --- | --- |
| Estado funcional | `verify` inclui JVM puro e HTTP com PostgreSQL; qualquer falha bloqueia | Autor e CI |
| Cobertura | JaCoCo geral e por policy, XML/HTML da revisão; Sonar importa XML. Em código novo, use ≥80% conforme o gate configurado | Autor e mantenedor do Sonar |
| Mutação | `MonthlyCreditPolicy`, 80% de score, 80% de linhas executadas no PIT, pelo menos um mutante e relatório saudável | Autor e CI |
| Estática | Perfil Java revisado, limite inicial de complexidade ciclomática 10 se S1541 disponível. Gate com zero novas issues, cobertura/duplicação e segurança conforme a edição | Mantenedor do Sonar |
| Revisão humana | Regra, risco, segurança e transações do diff aprovados por revisor independente | Revisor |
| Entrega | SHA elegível de `final`, gates e staging aprovados, mesmo digest e verificação pós-deploy | Plataforma e aprovador de produção |

Registre exceções com escopo, impacto, responsável, revisão futura e forma de verificação. Uma exceção vira ajuste deliberado após revisão. Mantenha dívida anterior em backlog, sem pretender que uma nota de manutenibilidade mede ausência de problemas. Complexidade cognitiva, ciclomática e duplicação orientam leitura e discussão sobre as causas. Os limiares de PIT e da regra S1541 são decisões da equipe para esta aula.

Em produção, monitore disponibilidade/readiness e erros HTTP por versão implantada. Resgates concorrentes devem preservar saldo e estoque. Uma falha de rollback exige verificar réplicas, versão do schema e dados antes de decidir como recuperar.
