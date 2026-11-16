# Critérios de aceitação

| Critério | Estado observado |
| --- | --- |
| 1. Baseline | `main` retém a comparação exclusiva `> 10`, identificada no README. |
| 2. Histórico | Seis branches ancestrais, commits distintos e tags no roteiro. |
| 3. Aplicação executável | Maven empacotou o fast-jar; a imagem JVM subiu com PostgreSQL e respondeu ao smoke. |
| 4. Regra de crédito | Suíte final cobre tabela, negativo e overflow de entrada extrema. |
| 5. Testes fracos e cobertura | Nove passam. A fase 01 mede os mesmos testes e regra em 100% das linhas da classe alvo e 79,6% das linhas do projeto. |
| 6. Sobrevivente inicial | 5 sobreviventes. Classe, linha e alteração da comparação constam no [registro](README.md). |
| 7. Fronteira | Fase 03 retorna 100 e falha esperando 150. Fase 04 e `final` passam com 150. |
| 8. PIT final | 7/7 mortos, score e cobertura PIT de 100%, limiares explícitos de 80 e configuração de erro quando o relatório é vazio. |
| 9. Regras HTTP e transações | Integração real cobre autorização, titularidade, competência, preço congelado, entrada inválida, estados de saldo/estoque e concorrência. |
| 10. Coverage e Sonar do SHA | XML e HTML gerados. Sonar local importou o XML. Workflow transfere classes e relatórios da mesma execução e tentativa. |
| 11. Bloqueios | Scanner local esperou o gate e propagou reprovação. PIT bloqueou a baseline por score de 29%. O agregador local rejeitou falha, cancelamento e pulo. |
| 12. Configuração ausente | O workflow falha explicitamente com token, URL, projeto ou XML ausente. Sintaxe e expressões dos workflows verificadas com actionlint. A execução remota permanece pendente. |
| 13. Revisão e GitHub | Template, checklist, CODEOWNERS de exemplo e guia de ruleset prontos. Revisores e configuração do GitHub dependem da conta. |
| 14. Promoção | O código vincula SHA, execução aprovada, manifesto e digest testado em staging. A ref `final` é verificada no dispatch. Política do Environment e aprovadores dependem da configuração externa. Fixtures validaram a resolução e a reversão localmente. |
| 15. Arquivos e segredos | `conteudo/`, `target/`, chaves, `.env` e artefatos locais são ignorados. As credenciais versionadas são apenas exemplos descartáveis. Imagens e ferramentas estão fixadas. |
| 16. Versões e fontes | [Matriz](../versoes-e-fontes.md) documenta versões, digests e fontes oficiais. |
