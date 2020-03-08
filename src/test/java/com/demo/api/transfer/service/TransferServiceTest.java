package com.demo.api.transfer.service;

import com.demo.api.account.model.Account;
import com.demo.api.account.service.AccountService;
import com.demo.api.model.AccountId;
import com.demo.api.model.TransferId;
import com.demo.api.transfer.model.Transfer;
import com.demo.common.InvalidDataException;
import com.demo.common.TooBusyException;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TransferServiceTest {
    private AccountService accountService = new AccountService() {
        @Override
        public AccountId createNew(BigDecimal initialBalance) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Account findById(AccountId accountId) {
            throw new UnsupportedOperationException();
        }
    };
    private TransferService transferService = new TransferService() {
        @Override
        public TransferId createNew(AccountId sourceAccountId, AccountId targetAccountId, BigDecimal amount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Transfer findById(TransferId transferId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Transfer> findAll() {
            throw new UnsupportedOperationException();
        }
    };

    @Test
    public void shouldAcceptAndProcessTransferIfSenderWithSufficientBalance() {
        // Given
        AccountId sourceAccountId = accountService.createNew(BigDecimal.ONE);
        AccountId targetAccountId = accountService.createNew(BigDecimal.ZERO);
        BigDecimal amount = BigDecimal.ONE;

        // When
        TransferId transferId = transferService.createNew(sourceAccountId, targetAccountId, amount);

        // Then
        Transfer transfer = transferService.findById(transferId);
        assertThat(transfer.getSourceId(), equalTo(sourceAccountId));
        assertThat(transfer.getTargetId(), equalTo(targetAccountId));
        assertThat(transfer.getAmount(), equalTo(amount));
    }

    @Test(expected = InvalidDataException.class)
    public void shouldRejectTransferIfSenderTransferMoreThanAvailable() {
        // Given
        AccountId sourceAccountId = accountService.createNew(BigDecimal.ONE);
        AccountId targetAccountId = accountService.createNew(BigDecimal.ZERO);
        BigDecimal amount = BigDecimal.TEN;

        // When
        transferService.createNew(sourceAccountId, targetAccountId, amount); // exception should be thrown
    }

    @Test(expected = InvalidDataException.class)
    public void shouldRejectTransferIfSenderSendZeroAmount() {
        // Given
        AccountId sourceAccountId = accountService.createNew(BigDecimal.ONE);
        AccountId targetAccountId = accountService.createNew(BigDecimal.ZERO);
        BigDecimal amount = BigDecimal.ZERO;

        // When
        transferService.createNew(sourceAccountId, targetAccountId, amount); // exception should be thrown
    }

    @Test(expected = InvalidDataException.class)
    public void shouldRejectTransferIfSenderSendNegativeAmount() {
        // Given
        AccountId sourceAccountId = accountService.createNew(BigDecimal.ONE);
        AccountId targetAccountId = accountService.createNew(BigDecimal.ZERO);
        BigDecimal amount = BigDecimal.ONE.negate();

        // When
        transferService.createNew(sourceAccountId, targetAccountId, amount); // exception should be thrown
    }

    @Test(expected = InvalidDataException.class)
    public void shouldRejectTransferIfSenderAndRecipientSameAccounts() {
        // Given
        AccountId sourceAccountId = accountService.createNew(BigDecimal.TEN);
        BigDecimal amount = BigDecimal.ONE;

        // When
        transferService.createNew(sourceAccountId, sourceAccountId, amount); // exception should be thrown
    }

    @Test(expected = TooBusyException.class)
    public void shouldRejectTransferIfAboveThreshold() {
        // Given
        AccountId sourceAccountId = accountService.createNew(BigDecimal.TEN);
        AccountId targetAccountId = accountService.createNew(BigDecimal.ZERO);
        BigDecimal amount = BigDecimal.ONE;

        // When
        transferService.createNew(sourceAccountId, targetAccountId, amount);
        transferService.createNew(sourceAccountId, targetAccountId, amount); // exception should be thrown
    }
}
