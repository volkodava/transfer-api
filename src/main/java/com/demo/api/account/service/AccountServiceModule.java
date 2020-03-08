package com.demo.api.account.service;

import com.google.inject.AbstractModule;

public class AccountServiceModule extends AbstractModule {

    protected void configure() {
        bind(AccountService.class).to(InMemoryAccountService.class);
    }
}
