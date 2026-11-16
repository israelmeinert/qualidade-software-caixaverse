package br.com.caixaverse.rewards.view.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record RedemptionRequest(
        @NotNull @Positive Long productId,
        @NotNull @Positive Integer quantity,
        @Pattern(regexp = "[\\p{L}\\p{M}\\p{N} .'-]{1,40}") String personalization) {
}
