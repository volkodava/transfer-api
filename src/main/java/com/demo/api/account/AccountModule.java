package com.demo.api.account;

import com.demo.api.account.repository.AccountRepositoryModule;
import com.demo.api.account.service.AccountServiceModule;
import com.demo.api.account.validator.AccountValidatorModule;
import com.google.inject.AbstractModule;

public class AccountModule extends AbstractModule {

    protected void configure() {
        install(new AccountRepositoryModule());
        install(new AccountValidatorModule());
        install(new AccountServiceModule());
    }
}
