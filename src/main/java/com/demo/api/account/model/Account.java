package com.demo.api.account.model;

import com.demo.api.model.AccountId;

import java.math.BigDecimal;
import java.util.Objects;

public final class Account {
    private final AccountId id;
    private final BigDecimal balance;

    private Account(AccountId id, BigDecimal balance) {
        this.id = Objects.requireNonNull(id, "Id must be provided");
        this.balance = Objects.requireNonNull(balance, "Balance must be provided");
    }

    public static Account copyOf(Account orig) {
        if (orig == null) {
            return null;
        }
        return new Account(orig.id, orig.balance);
    }

    public static Builder builder() {
        return new Builder();
    }

    public AccountId getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return id + "=" + balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static final class Builder {
        private AccountId id;
        private BigDecimal balance;

        public Builder withId(AccountId id) {
            this.id = id;
            return this;
        }

        public Builder withBalance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public Account build() {
            return new Account(id, balance);
        }
    }
}
