package finance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class WalletTest {
    @Test
    void calculatesTotalsCategoriesAndRemainingBudget() {
        Wallet wallet = new Wallet();
        wallet.addCategory("Еда");
        wallet.addCategory("Такси");
        wallet.getCategories().get("Еда").setBudget(new BigDecimal("1000.00"));
        wallet.addOperation(OperationType.EXPENSE, "Еда", new BigDecimal("300.00"));
        wallet.addOperation(OperationType.EXPENSE, "Еда", new BigDecimal("500.00"));
        wallet.addOperation(OperationType.EXPENSE, "Такси", new BigDecimal("150.00"));
        wallet.addOperation(OperationType.INCOME, "Зарплата", new BigDecimal("2000.00"));

        assertThat(wallet.total(OperationType.INCOME)).isEqualByComparingTo("2000");
        assertThat(wallet.total(OperationType.EXPENSE)).isEqualByComparingTo("950");
        assertThat(wallet.amountByCategory(OperationType.EXPENSE, "Еда")).isEqualByComparingTo("800");
        assertThat(wallet.createReport(List.of())).contains(
                "Общий доход: 2,000.00",
                "Общие расходы: 950.00",
                "Еда: 1,000.00, Оставшийся бюджет: 200.00");
    }

    @Test
    void createsSelectedCategoryReportAndRejectsUnknownCategories() {
        Wallet wallet = new Wallet();
        wallet.addCategory("Еда");
        wallet.addOperation(OperationType.EXPENSE, "Еда", new BigDecimal("100.00"));
        wallet.addOperation(OperationType.INCOME, "Бонус", new BigDecimal("50.00"));

        assertThat(wallet.createReport(List.of("Еда"))).contains("Еда: 100.00").doesNotContain("Бонус: 50.00");
        assertThatIllegalArgumentException().isThrownBy(() -> wallet.createReport(List.of("Нет такой")))
                .withMessage("категория не найдена: Нет такой");
    }
}
