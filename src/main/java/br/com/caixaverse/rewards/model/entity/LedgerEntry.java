package br.com.caixaverse.rewards.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entry")
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "employee_id", length = 80)
    public String employeeId;

    public long points;

    @Column(name = "monthly_credit_id")
    public Long monthlyCreditId;

    @Column(name = "redemption_id")
    public UUID redemptionId;

    @Column(name = "created_at")
    public Instant createdAt;
}
