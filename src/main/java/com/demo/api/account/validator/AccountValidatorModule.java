package com.demo.api.account.validator;

import com.google.inject.AbstractModule;

public class AccountValidatorModule extends AbstractModule {

    protected void configure() {
        bind(AccountValidator.class).to(DebitAccountValidator.class);
    }
}
