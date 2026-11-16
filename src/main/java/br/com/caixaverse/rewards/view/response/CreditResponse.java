package br.com.caixaverse.rewards.view.response;

import br.com.caixaverse.rewards.model.entity.MonthlyCredit;
import java.time.YearMonth;

public record CreditResponse(long id, String employeeId, YearMonth competence, int deliveries, int points) {
    public static CreditResponse from(MonthlyCredit entity) {
        return new CreditResponse(entity.id,
                entity.employeeId,
                entity.competence,
                entity.deliveries,
                entity.points);
    }
}
