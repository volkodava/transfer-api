package com.demo.api.account.service;

import com.demo.api.account.model.Account;
import com.demo.api.account.repository.AccountRepository;
import com.demo.api.account.validator.AccountValidator;
import com.demo.api.model.AccountId;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Singleton
public class InMemoryAccountService implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountValidator accountValidator;

    @Inject
    public InMemoryAccountService(AccountValidator accountValidator,
                                  AccountRepository accountRepository) {
        this.accountValidator = Objects.requireNonNull(accountValidator, "Account validator must be provided");
        this.accountRepository = Objects.requireNonNull(accountRepository, "Account repository must be provided");
    }

    @Override
    public AccountId createNew(BigDecimal initialBalance) {
        accountValidator.validate(initialBalance);

        AccountId accountId = AccountId.valueOf(UUID.randomUUID().toString());
        Account account = Account.builder()
                .withId(accountId)
                .withBalance(initialBalance)
                .build();
        accountRepository.save(accountId, account);

        return accountId;
    }

    @Override
    public Account findById(AccountId accountId) {
        Objects.requireNonNull(accountId, "Account id must be provided");

        return accountRepository.findById(accountId);
    }
}
