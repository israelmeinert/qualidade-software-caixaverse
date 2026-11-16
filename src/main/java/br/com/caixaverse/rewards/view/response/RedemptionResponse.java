package br.com.caixaverse.rewards.view.response;

import br.com.caixaverse.rewards.model.entity.Redemption;
import java.util.UUID;

public record RedemptionResponse(UUID id, long productId, int quantity, String personalization,
                                 int unitPrice, long totalPoints, String state) {
    public static RedemptionResponse from(Redemption entity) {
        return new RedemptionResponse(entity.id,
                entity.productId,
                entity.quantity,
                entity.personalization,
                entity.unitPrice,
                (long) entity.unitPrice * entity.quantity,
                entity.state.name());
    }
}
