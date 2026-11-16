package br.com.caixaverse.rewards.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(length = 40)
    public String sku;

    @Column(length = 160)
    public String description;

    public int price;
    public int stock;
    public boolean active;
    public boolean customizable;
}
