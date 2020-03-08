package com.demo.api.account.model;

import com.demo.api.model.Id;

public final class AccountId extends Id {
    public AccountId(String value) {
        super(value);
    }

    public static AccountId valueOf(String val) {
        return new AccountId(val);
    }
}
