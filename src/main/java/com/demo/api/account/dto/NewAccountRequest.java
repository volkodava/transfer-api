package com.demo.api.account.dto;

import java.math.BigDecimal;

public class NewAccountRequest {
    private BigDecimal initialBalance;

    public NewAccountRequest() {
    }

    public NewAccountRequest(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
