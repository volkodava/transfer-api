package com.demo.api.transfer.validator;

import com.demo.api.account.model.Account;
import com.demo.api.account.service.AccountService;
import com.demo.api.model.AccountId;
import com.demo.common.InvalidDataException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.util.Objects;

@Singleton
public class DebitTransferValidator implements TransferValidator {
    private final AccountService accountService;

    @Inject
    public DebitTransferValidator(AccountService accountService) {
        this.accountService = Objects.requireNonNull(accountService, "Account service must be provided");
    }

    @Override
    public void validate(AccountId sourceAccountId, AccountId targetAccountId, BigDecimal amount) {
        Objects.requireNonNull(sourceAccountId, "Source account id must be provided");
        Objects.requireNonNull(targetAccountId, "Target account id must be provided");
        Objects.requireNonNull(amount, "Amount must be provided");

        Account source = Objects.requireNonNull(accountService.findById(sourceAccountId), "Account doesn't exist");
        Account target = Objects.requireNonNull(accountService.findById(targetAccountId), "Account doesn't exist");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDataException("Transfer must be a positive decimal number");
        }
        if (source.getBalance().compareTo(amount) < 0) {
            throw new InvalidDataException("Insufficient account balance to execute transfer");
        }
        if (source.equals(target)) {
            throw new InvalidDataException("Source and target accounts must not be the same");
        }
    }
}
