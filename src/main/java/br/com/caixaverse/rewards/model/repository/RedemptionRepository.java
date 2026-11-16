package br.com.caixaverse.rewards.model.repository;

import br.com.caixaverse.rewards.model.entity.Redemption;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RedemptionRepository implements PanacheRepositoryBase<Redemption, UUID> {
    public Optional<Redemption> byKey(String employeeId, String key) {
        return find("employeeId = ?1 and idempotencyKey = ?2", employeeId, key).firstResultOptional();
    }

    public Optional<Redemption> owned(UUID id, String employeeId) {
        return find("id = ?1 and employeeId = ?2", id, employeeId).firstResultOptional();
    }
}
