package br.com.caixaverse.rewards.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "redemption")
public class Redemption {
    @Id
    public UUID id;

    @Column(name = "employee_id", length = 80)
    public String employeeId;

    @Column(name = "idempotency_key", length = 80)
    public String idempotencyKey;

    @Column(name = "product_id")
    public long productId;

    public int quantity;

    @Column(length = 40)
    public String personalization;

    @Column(name = "unit_price")
    public int unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    public State state;

    public enum State { CONFIRMED }
}
