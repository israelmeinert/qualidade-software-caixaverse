package br.com.caixaverse.rewards.view.response;

public record ErrorResponse(String code, String message, String correlationId) {
}
