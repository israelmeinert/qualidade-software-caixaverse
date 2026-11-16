package br.com.caixaverse.rewards.view.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Pattern(regexp = "[A-Z0-9_-]{1,40}") String sku,
        @NotBlank @Size(max = 160) String description,
        @NotNull @Positive Integer price,
        @NotNull @PositiveOrZero Integer stock,
        @NotNull Boolean active,
        @NotNull Boolean customizable) {
}
