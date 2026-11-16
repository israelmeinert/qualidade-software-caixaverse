package br.com.caixaverse.rewards.view.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record CreditRequest(
        @NotNull @Pattern(regexp = "[0-9]{4}-(0[1-9]|1[0-2])") String competence,
        @NotNull @PositiveOrZero Integer deliveries) {
}
