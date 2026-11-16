package br.com.caixaverse.rewards.model.repository;

import br.com.caixaverse.rewards.model.entity.Wallet;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@ApplicationScoped
public class WalletRepository implements PanacheRepositoryBase<Wallet, String> {
    public void ensureExists(String employeeId) {
        getEntityManager().createNativeQuery("""
                INSERT INTO wallet(employee_id, balance) VALUES (:employeeId, 0)
                ON CONFLICT (employee_id) DO NOTHING
                """).setParameter("employeeId", employeeId).executeUpdate();
    }

    public Optional<Wallet> lock(String employeeId) {
        return Optional.ofNullable(findById(employeeId, LockModeType.PESSIMISTIC_WRITE));
    }

    public void limitLockWait() {
        getEntityManager().createNativeQuery("SELECT set_config('lock_timeout', '3s', true)")
                .getSingleResult();
    }
}
