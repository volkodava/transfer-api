package com.demo.api.account.validator;

import java.math.BigDecimal;

public interface AccountValidator {
    void validate(BigDecimal initialBalance);
}
