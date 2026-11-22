package finance.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class Money {
    private Money() {
    }

    public static BigDecimal parsePositive(String value) {
        try {
            BigDecimal result = new BigDecimal(value.replace(',', '.'));
            if (result.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
            return result.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("сумма должна быть положительным числом");
        }
    }

    public static String format(BigDecimal value) {
        return String.format(Locale.US, "%,.2f", value);
    }
}
