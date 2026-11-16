package br.com.caixaverse.rewards.view.response;

import br.com.caixaverse.rewards.model.entity.LedgerEntry;
import java.util.UUID;
import java.time.Instant;

public record LedgerResponse(long id, long points, Long monthlyCreditId, UUID redemptionId, Instant createdAt) {
    public static LedgerResponse from(LedgerEntry entity) {
        return new LedgerResponse(entity.id,
                entity.points,
                entity.monthlyCreditId,
                entity.redemptionId,
                entity.createdAt);
    }
}
