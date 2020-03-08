package com.demo.api.account.dto;

import java.math.BigDecimal;

public class AccountResponse {
    private String id;
    private BigDecimal balance;

    public AccountResponse() {
    }

    public AccountResponse(String id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
