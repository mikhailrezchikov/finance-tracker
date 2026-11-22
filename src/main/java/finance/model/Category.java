package finance.model;

import java.io.Serializable;
import java.math.BigDecimal;

public final class Category implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private BigDecimal budget;

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
}
