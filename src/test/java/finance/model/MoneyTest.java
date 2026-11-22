package finance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {
    @Test
    void parsesPositiveAmountsWithCommaAndFormatsThousands() {
        BigDecimal amount = Money.parsePositive("1234,5");

        assertThat(amount).isEqualByComparingTo("1234.50");
        assertThat(Money.format(amount)).isEqualTo("1,234.50");
    }

    @Test
    void rejectsZeroNegativeAndNonNumericAmounts() {
        assertThatIllegalArgumentException().isThrownBy(() -> Money.parsePositive("0"));
        assertThatIllegalArgumentException().isThrownBy(() -> Money.parsePositive("-12"));
        assertThatIllegalArgumentException().isThrownBy(() -> Money.parsePositive("abc"));
    }
}
