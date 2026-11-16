package br.com.caixaverse.rewards.model.policy;

import br.com.caixaverse.rewards.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlyCreditPolicyTest {
    private final MonthlyCreditPolicy policy = new MonthlyCreditPolicy();

    // Baseline didática. O valor do crédito fica sem verificação.
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 9, 10, 11, 44, 45, 60})
    void calculatesWithoutThrowing(int deliveries) {
        assertDoesNotThrow(() -> policy.calculate(deliveries));
    }

    @Test
    void rejectsNegativeDeliveries() {
        assertThrows(BusinessException.class, () -> policy.calculate(-1));
    }
}
