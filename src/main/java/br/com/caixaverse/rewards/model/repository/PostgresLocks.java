package br.com.caixaverse.rewards.model.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class PostgresLocks {
    private final EntityManager database;

    public PostgresLocks(EntityManager database) {
        this.database = database;
    }

    public void setTimeoutForCurrentTransaction() {
        database.createNativeQuery("SELECT set_config('lock_timeout', '3s', true)").getSingleResult();
    }
}
