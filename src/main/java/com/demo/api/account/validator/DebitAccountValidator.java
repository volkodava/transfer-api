package com.demo.api.account.validator;

import com.demo.api.exception.InvalidDataException;
import com.google.inject.Singleton;

import java.math.BigDecimal;

@Singleton
public class DebitAccountValidator implements AccountValidator {
    @Override
    public void validate(BigDecimal initialBalance) {
        if (initialBalance == null) {
            throw new InvalidDataException("Account balance must be provided");
        }
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Account must have positive balance");
        }
    }
}
