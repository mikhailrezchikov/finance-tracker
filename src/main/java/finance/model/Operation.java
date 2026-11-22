package finance.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Operation implements Serializable {
    private static final long serialVersionUID = 1L;
    private final OperationType type;
    private final String category;
    private final BigDecimal amount;
    private final LocalDateTime createdAt;

    public Operation(OperationType type, String category, BigDecimal amount, LocalDateTime createdAt) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public OperationType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
