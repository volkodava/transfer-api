package com.demo.api.account.service;

import com.demo.api.account.model.Account;
import com.demo.api.account.model.AccountId;

import java.math.BigDecimal;

public interface AccountService {
    AccountId createNew(BigDecimal initialBalance);

    Account findById(AccountId accountId);
}
