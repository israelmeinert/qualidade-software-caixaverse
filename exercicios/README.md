# Exercícios

1. **Cobertura e aceite.** Todos os testes passam e a cobertura de linhas da policy supera 80%. O que ainda precisa ser comprovado? Examine as asserções de `main`, o valor para 10 entregas e o escopo do relatório. Proponha dois testes com expectativas independentes.
2. **Sobrevivente real.** Na branch `02-mutacao-diagnostico`, abra o relatório PIT de `MonthlyCreditPolicy`. Encontre a alteração de fronteira condicional da comparação das 10 entregas. Escreva um teste que distinga os comportamentos e explique o primeiro resultado antes da mutação.
3. **Resgate concorrente.** Revise as transações de `RedemptionService`. Uma carteira tem 90 pontos, dois pedidos de 60 chegam juntos e o catálogo tem dois produtos distintos. O que deve acontecer com saldo, estoque, extrato e pedidos? Repita com duas carteiras, último item e a mesma chave em cada carteira. Descreva a restrição necessária e um teste para cada risco.
4. **Promoção.** O job de imagem foi aprovado, staging foi pulado e o operador apresenta um digest com formato correto. Aponte qual evidência falta e abra o código que rejeita a promoção.
