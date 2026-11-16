package br.com.caixaverse.rewards.configuration;

import jakarta.enterprise.context.RequestScoped;

import java.util.UUID;

@RequestScoped
public class Correlation {
    private final String id = UUID.randomUUID().toString();

    public String id() {
        return id;
    }
}
