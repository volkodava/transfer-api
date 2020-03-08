package com.demo.api.transfer.model;

import com.demo.api.model.AccountId;
import com.demo.api.model.TransferId;

import java.math.BigDecimal;
import java.util.Objects;

public final class TransferEvent {
    private final TransferId id;
    private final AccountId sourceId;
    private final AccountId targetId;
    private final BigDecimal amount;
    private TransferState state;
    private String details;

    private TransferEvent(TransferId id, AccountId sourceId, AccountId targetId, BigDecimal amount,
                          TransferState state, String details) {
        this.id = Objects.requireNonNull(id, "Id must be provided");
        this.sourceId = Objects.requireNonNull(sourceId, "Source account id must be provided");
        this.targetId = Objects.requireNonNull(targetId, "Target account id must be provided");
        this.amount = Objects.requireNonNull(amount, "Amount must be provided");
        this.state = Objects.requireNonNull(state, "State must be provided");
        this.details = Objects.requireNonNull(details, "Details must be provided");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Transfer asTransfer() {
        return Transfer.builder()
                .withId(id)
                .withSourceId(sourceId)
                .withTargetId(targetId)
                .withAmount(amount)
                .withState(state)
                .withDetails(details)
                .build();
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

    public void setState(TransferState state) {
        this.state = state;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
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
        TransferEvent that = (TransferEvent) o;
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

        public Builder withId(TransferId id) {
            this.id = id;
            return this;
        }

        public Builder withSourceId(AccountId sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder withTargetId(AccountId targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder withAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder withState(TransferState state) {
            this.state = state;
            return this;
        }

        public Builder withDetails(String details) {
            this.details = details;
            return this;
        }

        public TransferEvent build() {
            return new TransferEvent(id, sourceId, targetId, amount, state, details);
        }
    }
}
