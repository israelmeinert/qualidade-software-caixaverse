package br.com.caixaverse.rewards.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    @Column(name = "employee_id", length = 80)
    public String employeeId;

    public long balance;
}
