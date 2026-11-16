# Checklist de code review

Antes do diff, leia a regra, critérios de aceite e riscos. Siga uma requisição do controller ao banco e volte pelos DTOs. Priorize o diff de maior impacto. Leia asserções e relatórios do SHA em revisão e reproduza cenários críticos. Marque claramente bloqueios e sugestões.

| Dimensão | Perguntas de revisão |
| --- | --- |
| Negócio | 10 entregas recebem bônus? Retry duplica crédito? Preço vem do servidor e fica congelado? |
| MVC | Controller trata HTTP? A transação está no serviço? DTO impede exposição da entidade? A policy é testável sem infraestrutura? |
| Segurança | Papel e propriedade foram verificados? Dados são validados? Tokens e segredos ficam fora do histórico e dos logs? |
| Consistência | Saldo, estoque, pedido e extrato confirmam juntos? Há constraints reais? As corridas foram demonstradas? |
| Performance | Há paginação, índices e consultas sem N+1? A duração e a ordem dos locks são justificadas? |
| Legibilidade | Nomes explicam a regra? O método pode ser simplificado? A duplicação representa regra compartilhada? |
| Testes | Há asserções sobre resultados, erros e limites? Os testes executam o código real? Sobreviventes têm explicação? |
| Operação | Liveness, readiness e correlação são utilizáveis? Migração e reversão são compatíveis? |

**Bloqueio em MonthlyCreditPolicy.calculate**. A comparação exclui 10 entregas, que recebem 100 pontos. Use comparação inclusiva e um caso parametrizado que exija 150 para 10. Reexecute a suíte e o PIT no mesmo SHA.

**Sugestão em CatalogResource.list**. Considere paginação por cursor se a API precisar atravessar catálogos grandes. Registre o plano após medir.

Copie `.github/CODEOWNERS.example` para `.github/CODEOWNERS` após substituir os placeholders por membros ou times com acesso. Use recurso de revisão por owners e proteção do GitHub conforme as capacidades do plano. Exija revisão independente e invalide aprovações quando o diff mudar, onde a configuração permita.
