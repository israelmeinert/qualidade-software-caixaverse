# Roteiro da aula

| Fase | Comando ou tela | Pergunta e evidência |
| --- | --- | --- |
| `main` | Inicie a API e lance crédito com 10 entregas. Rode `./mvnw -B test`. | Por que 100 pontos coexistem com testes verdes? Examine as asserções fracas. |
| `01-sonar-ci` | `./mvnw -B -Pcoverage clean verify`, abra `target/jacoco-report/index.html`. Use o painel Sonar da branch principal, quando configurado. | A porcentagem da classe e a do projeto medem o mesmo conjunto? O gate pode descobrir a regra de negócio? |
| `02-mutacao-diagnostico` | Execute o PIT descrito em [mutação](mutacao.md) e abra o HTML/XML. | Qual comportamento alterado os testes ainda aceitam? |
| `03-testes-fortes` | `./mvnw -B clean test`. Mostre a asserção de exatamente 10 entregas. | Por que 150 vem da especificação? A falha de 100 ocorre no original, antes da mutação. |
| `04-correcao-qualidade` | Repita `verify` e execute PIT com `-Dpit.mutationThreshold=80`. | Qual caso distingue a fronteira corrigida? Qual efeito persistido as corridas verificam? |
| `final` | Abra o workflow de qualidade, o job de staging e o dispatch de produção. | Qual evidência prova que a produção receberia o mesmo digest? Compare jobs, SHA, manifesto e smoke. |

A fase **03** contém uma falha proposital. O PIT precisa da suíte original verde; use o relatório da fase anterior para leitura diagnóstica e regenere somente após a correção. Para evitar vazamento de relatórios entre fases, use `clean` ao trocar de branch.

Reserve discussão para o [checklist de revisão](checklist-code-review.md). Compare uma issue do Sonar com o erro de fronteira e conduza os exercícios antes dos gabaritos. Cite os números do repositório e seu escopo.
