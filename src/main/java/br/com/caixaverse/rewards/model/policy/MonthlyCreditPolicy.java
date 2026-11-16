package br.com.caixaverse.rewards.model.policy;

import br.com.caixaverse.rewards.model.exception.BusinessException;

public class MonthlyCreditPolicy {
    public int calculate(int deliveries) {
        if (deliveries < 0) {
            throw BusinessException.invalid("O número de entregas deve ser não negativo");
        }
        long points = deliveries * 10L;
        if (deliveries > 10) { // Defeito intencional da aula. A especificação inclui 10.
            points += 50L;
        }
        return (int) Math.min(points, 500L);
    }
}
