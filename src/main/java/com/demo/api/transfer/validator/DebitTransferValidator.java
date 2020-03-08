package com.demo.api.transfer.validator;

import com.demo.api.account.model.Account;
import com.demo.api.account.service.AccountService;
import com.demo.api.account.model.AccountId;
import com.demo.api.exception.InvalidDataException;
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
        if (sourceAccountId == null) {
            throw new InvalidDataException("Source account id must be provided");
        }
        if (targetAccountId == null) {
            throw new InvalidDataException("Target account id must be provided");
        }
        if (amount == null) {
            throw new InvalidDataException("Amount must be provided");
        }

        Account source = accountService.findById(sourceAccountId);
        if (source == null) {
            throw new InvalidDataException("Source account not found");
        }
        Account target = accountService.findById(targetAccountId);
        if (target == null) {
            throw new InvalidDataException("Target account not found");
        }

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
