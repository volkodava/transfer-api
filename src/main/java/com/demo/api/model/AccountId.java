package com.demo.api.model;

public final class AccountId extends Id {
    public AccountId(String value) {
        super(value);
    }

    public static AccountId valueOf(String val) {
        return new AccountId(val);
    }
}
