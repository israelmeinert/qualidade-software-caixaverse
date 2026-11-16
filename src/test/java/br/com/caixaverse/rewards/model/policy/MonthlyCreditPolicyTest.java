package br.com.caixaverse.rewards.model.policy;

import br.com.caixaverse.rewards.model.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthlyCreditPolicyTest {
    private final MonthlyCreditPolicy policy = new MonthlyCreditPolicy();

    @ParameterizedTest(name = "{0} entregas valem {1} pontos")
    @CsvSource({
            "0,0", "1,10", "9,90", "10,150", "11,160",
            "44,490", "45,500", "46,500", "60,500", "2147483647,500"
    })
    void calculatesSpecifiedCredit(int deliveries, int expected) {
        assertEquals(expected, policy.calculate(deliveries));
    }

    @Test
    void rejectsNegativeDeliveries() {
        assertThrows(BusinessException.class, () -> policy.calculate(-1));
    }
}
