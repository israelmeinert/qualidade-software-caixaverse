package br.com.caixaverse.rewards.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.YearMonth;

@Entity
@Table(name = "monthly_credit")
public class MonthlyCredit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "employee_id", length = 80)
    public String employeeId;

    @Convert(converter = YearMonthConverter.class)
    @Column(length = 7)
    public YearMonth competence;

    public int deliveries;
    public int points;
}
