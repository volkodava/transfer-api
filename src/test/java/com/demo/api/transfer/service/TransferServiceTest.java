package com.demo.api.transfer.service;

import com.demo.api.account.model.Account;
import com.demo.api.account.service.AccountService;
import com.demo.api.model.AccountId;
import com.demo.api.transfer.manager.TransferManager;
import com.demo.api.transfer.model.TransferEvent;
import com.demo.api.transfer.repository.TransferRepository;
import com.demo.api.transfer.validator.DebitTransferValidator;
import com.demo.api.transfer.validator.TransferValidator;
import com.demo.common.InvalidDataException;
import com.demo.common.TooBusyException;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import net.lamberto.junit.GuiceJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules(TransferServiceTest.TestModule.class)
public class TransferServiceTest {

    @Inject
    private TransferManager transferManager;

    @Inject
    private AccountService accountService;

    @Inject
    private TransferService transferService;

    @Test
    public void shouldAcceptAndProcessTransferIfSenderWithSufficientBalance() {
        // Given
        BigDecimal amount = BigDecimal.ONE;
        Account sourceAccount = Account.builder()
                .withId(AccountId.valueOf("1"))
                .withBalance(BigDecimal.ONE)
                .build();
        Account targetAccount = Account.builder()
                .withId(AccountId.valueOf("2"))
                .withBalance(BigDecimal.ZERO)
                .build();
        when(accountService.findById(sourceAccount.getId())).thenReturn(sourceAccount);
        when(accountService.findById(targetAccount.getId())).thenReturn(targetAccount);
        when(transferManager.submitEvent(any(TransferEvent.class))).thenReturn(true);

        // When
        transferService.createNew(sourceAccount.getId(), targetAccount.getId(), amount);

        // Then
        verify(transferManager, times(1)).submitEvent(any(TransferEvent.class));
    }

    @Test
    public void shouldRejectTransferIfSenderTransferMoreThanAvailable() {
        // Given
        BigDecimal amount = BigDecimal.TEN;
        Account sourceAccount = Account.builder()
                .withId(AccountId.valueOf("1"))
                .withBalance(BigDecimal.ONE)
                .build();
        Account targetAccount = Account.builder()
                .withId(AccountId.valueOf("2"))
                .withBalance(BigDecimal.ZERO)
                .build();
        when(accountService.findById(sourceAccount.getId())).thenReturn(sourceAccount);
        when(accountService.findById(targetAccount.getId())).thenReturn(targetAccount);
        when(transferManager.submitEvent(any(TransferEvent.class))).thenReturn(true);

        // When
        try {
            transferService.createNew(sourceAccount.getId(), targetAccount.getId(), amount); // exception should be thrown
            fail("Expected exception to be thrown.");
        } catch (InvalidDataException expected) {
        }

        // Then
        verify(transferManager, never()).submitEvent(any(TransferEvent.class));
    }

    @Test
    public void shouldRejectTransferIfSenderSendZeroAmount() {
        // Given
        BigDecimal amount = BigDecimal.ZERO;
        Account sourceAccount = Account.builder()
                .withId(AccountId.valueOf("1"))
                .withBalance(BigDecimal.ONE)
                .build();
        Account targetAccount = Account.builder()
                .withId(AccountId.valueOf("2"))
                .withBalance(BigDecimal.ZERO)
                .build();
        when(accountService.findById(sourceAccount.getId())).thenReturn(sourceAccount);
        when(accountService.findById(targetAccount.getId())).thenReturn(targetAccount);
        when(transferManager.submitEvent(any(TransferEvent.class))).thenReturn(true);

        // When
        try {
            transferService.createNew(sourceAccount.getId(), targetAccount.getId(), amount); // exception should be thrown
            fail("Expected exception to be thrown.");
        } catch (InvalidDataException expected) {
        }

        // Then
        verify(transferManager, never()).submitEvent(any(TransferEvent.class));
    }

    @Test
    public void shouldRejectTransferIfSenderSendNegativeAmount() {
        // Given
        BigDecimal amount = BigDecimal.ONE.negate();
        Account sourceAccount = Account.builder()
                .withId(AccountId.valueOf("1"))
                .withBalance(BigDecimal.ONE)
                .build();
        Account targetAccount = Account.builder()
                .withId(AccountId.valueOf("2"))
                .withBalance(BigDecimal.ZERO)
                .build();
        when(accountService.findById(sourceAccount.getId())).thenReturn(sourceAccount);
        when(accountService.findById(targetAccount.getId())).thenReturn(targetAccount);
        when(transferManager.submitEvent(any(TransferEvent.class))).thenReturn(true);

        // When
        try {
            transferService.createNew(sourceAccount.getId(), targetAccount.getId(), amount); // exception should be thrown
            fail("Expected exception to be thrown.");
        } catch (InvalidDataException expected) {
        }

        // Then
        verify(transferManager, never()).submitEvent(any(TransferEvent.class));
    }

    @Test
    public void shouldRejectTransferIfSenderAndRecipientSameAccounts() {
        // Given
        BigDecimal amount = BigDecimal.ONE;
        Account sourceAccount = Account.builder()
                .withId(AccountId.valueOf("1"))
                .withBalance(BigDecimal.ONE)
                .build();
        when(accountService.findById(sourceAccount.getId())).thenReturn(sourceAccount);
        when(transferManager.submitEvent(any(TransferEvent.class))).thenReturn(true);

        // When
        try {
            transferService.createNew(sourceAccount.getId(), sourceAccount.getId(), amount); // exception should be thrown
            fail("Expected exception to be thrown.");
        } catch (InvalidDataException expected) {
        }

        // Then
        verify(transferManager, never()).submitEvent(any(TransferEvent.class));
    }

    @Test
    public void shouldRejectTransferIfAboveThreshold() {
        // Given
        BigDecimal amount = BigDecimal.ONE;
        Account sourceAccount = Account.builder()
                .withId(AccountId.valueOf("1"))
                .withBalance(BigDecimal.TEN)
                .build();
        Account targetAccount = Account.builder()
                .withId(AccountId.valueOf("2"))
                .withBalance(BigDecimal.ZERO)
                .build();
        when(accountService.findById(sourceAccount.getId())).thenReturn(sourceAccount);
        when(accountService.findById(targetAccount.getId())).thenReturn(targetAccount);
        when(transferManager.submitEvent(any(TransferEvent.class))).thenReturn(false); // can't accept more requests

        // When
        try {
            transferService.createNew(sourceAccount.getId(), targetAccount.getId(), amount); // exception should be thrown
            fail("Expected exception to be thrown.");
        } catch (TooBusyException expected) {
        }

        // Then
        verify(transferManager, times(1)).submitEvent(any(TransferEvent.class));
    }

    public static class TestModule extends AbstractModule {
        protected void configure() {
            configureAccountModule();
            configureTransferModule();
        }

        private void configureAccountModule() {
            // mock
            bind(AccountService.class).toInstance(mock(AccountService.class));
        }

        private void configureTransferModule() {
            // mock
            bind(TransferRepository.class).toInstance(mock(TransferRepository.class));
            bind(TransferValidator.class).to(DebitTransferValidator.class);
            // mock
            bind(TransferManager.class).toInstance(mock(TransferManager.class));
            bind(TransferService.class).to(InMemoryTransferService.class);
        }
    }
}
