# Diagnóstico de mutação

O PIT altera bytecode temporariamente. Ele executa a suíte original e testa alterações pequenas, como trocar o operador de uma fronteira. O arquivo Java continua igual. Um mutante morto indica que algum teste observou o efeito daquela alteração. A especificação ainda precisa ser validada por pessoas e testes de comportamento.

```bash
# Testes comuns e de integração, com cobertura e PostgreSQL.
./mvnw -B -Pcoverage clean verify
# Diagnóstico na branch 02, com limiar de score em zero.
./mvnw -B -Pmutation clean test-compile org.pitest:pitest-maven:1.30.2:mutationCoverage
# Ensaio do gate. A baseline deve reprovar o limite de 80.
./mvnw -B -Pmutation test-compile org.pitest:pitest-maven:1.30.2:mutationCoverage -Dpit.mutationThreshold=80
```

Abra `target/pit-reports/index.html` e confira classe, linha e alteração em `target/pit-reports/mutations.xml`. Os dois profiles são independentes. O PIT seleciona apenas `MonthlyCreditPolicy` e seu teste Jupiter puro. As integrações Quarkus, banco e CDI continuam em `verify`. O adaptador se chama `pitest-junit5-plugin`; nesta combinação ele roda Jupiter 6.1.3.

- `KILLED`: teste falhou ao observar a alteração.
- `SURVIVED`: a execução alcançou o mutante, mas os testes o aceitaram.
- `NO_COVERAGE`: nenhum teste selecionado exercitou a linha. Verifique a descoberta, o filtro e as entradas.
- `TIMED_OUT` e estados de erro: verifique laços alterados, limites de tempo, JVM, logs e incompatibilidades. Estados classificados como detectados pelo PIT podem inflar o score operacional. O gate final valida cada estado além do percentual.
- Zero mutantes significa configuração ou escopo a investigar. O Maven falha com `failWhenNoMutations=true`.

Com somente mortos, sobreviventes e sem cobertura, o score usa todos os mutantes gerados. Test strength divide os detectados pelos mutantes cobertos. Exemplo ilustrativo com 8 mortos, 2 sobreviventes e 5 sem cobertura: score 8/15 (≈53%) e força 8/10 (80%). O relatório real está documentado nas evidências.

## Investigar um sobrevivente

Leia a mudança em contexto e procure a menor entrada válida que diferencie o resultado. Se o limite do bônus mudar, teste exatamente 10 entregas com a expectativa independente de 150 pontos. Essa expectativa já falha no código inicial com 100 pontos, antes de rodar mutações. Corrija primeiro o código original, regenere e confira o estado do novo mutante de fronteira. Para um candidato a equivalente, argumente por quais entradas e efeitos a alteração seria indistinguível. Evite excluir uma classe inteira por causa de um caso.

O custo cresce com mutantes e testes. Mantenha o gate de PR focado em regras críticas com execução rápida. Uma análise periódica mais ampla pode revelar lacunas em outras áreas. Documente mudanças de filtros, operadores e versões para preservar a comparabilidade.

[Configuração Maven do PIT](https://pitest.org/quickstart/maven/) · [Mutadores](https://pitest.org/quickstart/mutators/) · [Conceitos e estados](https://pitest.org/quickstart/basic_concepts/).
