package com.demo.api.account.repository;

import com.demo.api.account.model.Account;
import com.demo.api.model.AccountId;

import java.util.function.BiFunction;

public interface AccountRepository {
    void save(AccountId accountId, Account account);

    Account findById(AccountId accountId);

    void executeUpdate(AccountId accountId, BiFunction<AccountId, Account, Account> executor);
}
