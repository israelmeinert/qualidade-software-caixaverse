# Arquitetura e regras operacionais

MVC REST em um processo. Controllers tratam contrato HTTP, papel e identidade. O model inclui entidades, repositórios Panache, serviços transacionais e a política Java pura. Os records de resposta são a view JSON. A API não usa templates HTML nem serializa entidades.

- `wallet` tem uma linha por colaborador e um saldo `bigint` com restrição de não negatividade.
- `monthly_credit` registra competência `YearMonth` (convertida para `AAAA-MM`), entregas e crédito. A chave única é colaborador + competência. A competência é informada explicitamente pelo operador, segundo o calendário de São Paulo.
- `redemption` guarda preço unitário histórico, quantidade, personalização e estado `CONFIRMED`. A chave única é colaborador + chave de idempotência.
- `ledger_entry` referencia exatamente um crédito ou pedido. A aplicação grava a contrapartida na mesma transação. O índice por colaborador e ID atende o extrato paginado. O catálogo tem índice parcial de produtos ativos.

## Transações e concorrência

Crédito cria a carteira com `ON CONFLICT DO NOTHING` e obtém bloqueio pessimista sobre ela. A consulta de competência acontece sob o bloqueio. Repetição idêntica devolve o lançamento original; quantidade diferente gera 409. Estornos e ajustes precisam de fluxo separado e auditável.

Resgate bloqueia carteira e depois produto. A busca pela chave é feita antes de consultar estoque e preço, permitindo a devolução do pedido original após mudança de preço ou esgotamento. Sem pedido anterior, somente o preço obtido sob bloqueio determina o débito. Multiplicação usa `long`. Estoque, saldo, pedido e extrato são confirmados juntos. A atualização de catálogo bloqueia apenas o produto. Em `READ COMMITTED`, os escritores que estavam esperando passam a enxergar o estado confirmado.

A espera de lock é limitada a 3 segundos por transação. Timeout, deadlock ou conflito de serialização devolvem 409; repetir a requisição com a mesma chave é seguro. As restrições no PostgreSQL são a última defesa para escritores que contornem o serviço. Uma violação de unicidade é tratada após rollback, fora da transação original. Consulte novamente em nova transação se for necessário distinguir colisão de replay; a API atual devolve 409 e deixa o cliente repetir. Manter o lock de carteira para todos os escritores evita essa colisão no fluxo normal.

## Fronteiras e segurança

O administrador representa uma entrada de entregas autorizada e fictícia. A API verifica assinatura/expiração, emissor e papéis do JWT. O `sub` é o identificador estável da carteira. Rotas `/me` derivam a identidade do token. A leitura do pedido filtra também o dono e responde 404 para pedido alheio. Erros REST incluem código, mensagem e ID de correlação; o logger omite conteúdo e tokens.

O catálogo é público e cada página tem até 100 registros. A personalização aceita letras, marcas diacríticas, números e pontuação limitada. O retorno é JSON; clientes web devem escapar texto antes de o exibir. A opção de retirar estoque pelo endpoint administrativo substitui o saldo de estoque explicitamente. Integração real de RH, trilha de estornos e reserva/entrega física ficam como evoluções do domínio.

Flyway aplica migrations versionadas, Hibernate valida e a produção exclui a localização de seed. A migração de dados fictícios em desenvolvimento/teste está numerada a partir de 9000. Para futuras migrations de schema abaixo de 9000, recrie bases descartáveis de dev/teste ou mantenha o seed em um schema histórico separado para evitar a lacuna de versões. Em produção, faça mudanças compatíveis com a imagem anterior para permitir reversão.
