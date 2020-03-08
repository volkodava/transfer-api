package com.demo.api.account.validator;

import com.demo.common.InvalidDataException;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.util.Objects;

@Singleton
public class DebitAccountValidator implements AccountValidator {
    @Override
    public void validate(BigDecimal initialBalance) {
        Objects.requireNonNull(initialBalance, "Account balance must be provided");
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Account must have positive balance");
        }
    }
}
