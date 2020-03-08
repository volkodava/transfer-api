package com.demo.api.account.repository;

import com.demo.api.account.model.Account;
import com.demo.api.account.model.AccountId;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

@Singleton
public class MapAccountRepository implements AccountRepository {

    private final Map<AccountId, Account> mapImpl;

    @Inject
    public MapAccountRepository(@Named("accountMapStorage") Map<AccountId, Account> mapStorage) {
        this.mapImpl = Objects.requireNonNull(mapStorage, "Map storage must be provided");
    }

    @Override
    public void save(AccountId key, Account value) {
        Objects.requireNonNull(key, "Key must be provided");
        Objects.requireNonNull(value, "Value must be provided");

        mapImpl.put(key, value);
    }

    @Override
    public Account findById(AccountId key) {
        Objects.requireNonNull(key, "Key must be provided");

        return mapImpl.get(key);
    }

    @Override
    public void executeUpdate(AccountId key, BiFunction<AccountId, Account, Account> executor) {
        Objects.requireNonNull(key, "Key must be provided");
        Objects.requireNonNull(executor, "Executor must be provided");

        mapImpl.compute(key, executor);
    }
}
