package com.demo.api.transfer.model;

import com.demo.api.model.AccountId;
import com.demo.api.model.TransferId;

import java.math.BigDecimal;
import java.util.Objects;

public final class Transfer {
    private final TransferId id;
    private final AccountId sourceId;
    private final AccountId targetId;
    private final BigDecimal amount;
    private final TransferState state;
    private final String details;

    private Transfer(TransferId id, AccountId sourceId, AccountId targetId, BigDecimal amount,
                     TransferState state, String details) {
        this.id = Objects.requireNonNull(id, "Id must be provided");
        this.sourceId = Objects.requireNonNull(sourceId, "Source account id must be provided");
        this.targetId = Objects.requireNonNull(targetId, "Target account id must be provided");
        this.amount = Objects.requireNonNull(amount, "Amount must be provided");
        this.state = Objects.requireNonNull(state, "State must be provided");
        this.details = Objects.requireNonNull(details, "Details must be provided");
    }

    public static Transfer.Builder builder() {
        return new Transfer.Builder();
    }

    public TransferId getId() {
        return id;
    }

    public AccountId getSourceId() {
        return sourceId;
    }

    public AccountId getTargetId() {
        return targetId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransferState getState() {
        return state;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "#" + id
                + ", Source=" + sourceId
                + ", Target=" + targetId
                + ", Amount=" + amount
                + ", State=" + state
                + ", Details=" + details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer that = (Transfer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static final class Builder {
        private TransferId id;
        private AccountId sourceId;
        private AccountId targetId;
        private BigDecimal amount;
        private TransferState state;
        private String details;

        public Transfer.Builder withId(TransferId id) {
            this.id = id;
            return this;
        }

        public Transfer.Builder withSourceId(AccountId sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Transfer.Builder withTargetId(AccountId targetId) {
            this.targetId = targetId;
            return this;
        }

        public Transfer.Builder withAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Transfer.Builder withState(TransferState state) {
            this.state = state;
            return this;
        }

        public Transfer.Builder withDetails(String details) {
            this.details = details;
            return this;
        }

        public Transfer build() {
            return new Transfer(id, sourceId, targetId, amount, state, details);
        }
    }
}
