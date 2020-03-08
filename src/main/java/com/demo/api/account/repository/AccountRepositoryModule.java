package com.demo.api.account.repository;

import com.demo.api.account.model.Account;
import com.demo.api.model.AccountId;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountRepositoryModule extends AbstractModule {

    protected void configure() {
        Map<AccountId, Account> accountMapStorage = new ConcurrentHashMap<>();
        bind(new TypeLiteral<Map<AccountId, Account>>() {
        }).annotatedWith(Names.named("accountMapStorage")).toInstance(accountMapStorage);
        bind(AccountRepository.class).to(MapAccountRepository.class);
    }
}
