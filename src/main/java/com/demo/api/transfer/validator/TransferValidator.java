package com.demo.api.transfer.validator;

import com.demo.api.model.AccountId;

import java.math.BigDecimal;

public interface TransferValidator {
    void validate(AccountId sourceAccountId, AccountId targetAccountId, BigDecimal amount);
}
