package finance.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Wallet implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, Category> categories = new LinkedHashMap<>();
    private final List<Operation> operations = new ArrayList<>();

    public boolean addCategory(String name) {
        return categories.putIfAbsent(name, new Category(name)) == null;
    }

    public Map<String, Category> getCategories() {
        return categories;
    }

    public void addOperation(OperationType type, String category, BigDecimal amount) {
        operations.add(new Operation(type, category, amount, LocalDateTime.now()));
    }

    public BigDecimal total(OperationType type) {
        return operations.stream().filter(operation -> operation.getType() == type).map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal amountByCategory(OperationType type, String category) {
        return operations.stream().filter(operation -> operation.getType() == type && operation.getCategory().equals(category))
                .map(Operation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean hasKnownCategory(String category) {
        return categories.containsKey(category) || operations.stream().anyMatch(operation -> operation.getCategory().equals(category));
    }

    public String createReport(List<String> selected) {
        for (String category : selected) {
            if (!hasKnownCategory(category)) {
                throw new IllegalArgumentException("категория не найдена: " + category);
            }
        }
        StringBuilder report = new StringBuilder();
        report.append("Общий доход: ").append(Money.format(total(OperationType.INCOME))).append(System.lineSeparator());
        appendOperations(report, "Доходы по категориям:", OperationType.INCOME, selected);
        report.append("Общие расходы: ").append(Money.format(total(OperationType.EXPENSE))).append(System.lineSeparator());
        appendOperations(report, "Расходы по категориям:", OperationType.EXPENSE, selected);
        report.append("Бюджет по категориям:").append(System.lineSeparator());
        categories.values().stream().sorted(Comparator.comparing(Category::getName)).forEach(category -> {
            if (category.getBudget() != null && matches(selected, category.getName())) {
                BigDecimal remaining = category.getBudget().subtract(amountByCategory(OperationType.EXPENSE, category.getName()));
                report.append(category.getName()).append(": ").append(Money.format(category.getBudget()))
                        .append(", Оставшийся бюджет: ").append(Money.format(remaining)).append(System.lineSeparator());
            }
        });
        return report.toString();
    }

    private void appendOperations(StringBuilder report, String title, OperationType type, List<String> selected) {
        report.append(title).append(System.lineSeparator());
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        operations.stream().filter(operation -> operation.getType() == type)
                .forEach(operation -> totals.merge(operation.getCategory(), operation.getAmount(), BigDecimal::add));
        totals.entrySet().stream().filter(entry -> matches(selected, entry.getKey())).sorted(Map.Entry.comparingByKey())
                .forEach(entry -> report.append(entry.getKey())
                        .append(": ")
                        .append(Money.format(entry.getValue()))
                        .append(System.lineSeparator()));
    }

    private boolean matches(List<String> selected, String category) {
        return selected.isEmpty() || selected.contains(category);
    }
}
