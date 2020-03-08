package com.demo.api.account.service;

import com.demo.api.account.model.Account;
import com.demo.api.account.repository.AccountRepository;
import com.demo.api.account.validator.AccountValidator;
import com.demo.api.account.validator.DebitAccountValidator;
import com.demo.api.model.AccountId;
import com.demo.common.InvalidDataException;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import net.lamberto.junit.GuiceJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules(AccountServiceTest.TestModule.class)
public class AccountServiceTest {

    @Inject
    private AccountRepository accountRepository;

    @Inject
    private AccountService accountService;

    @Test
    public void shouldCreateAccountWithPositiveBalance() {
        // Given
        BigDecimal positiveBalance = BigDecimal.ONE;

        // When
        AccountId sourceAccountId = accountService.createNew(positiveBalance);

        // Then
        assertThat(sourceAccountId, notNullValue());
        assertThat(sourceAccountId.getValue(), not(isEmptyOrNullString()));
        verify(accountRepository, times(1)).save(eq(sourceAccountId), any(Account.class));
    }

    @Test
    public void shouldCreateAccountWithZeroBalance() {
        // Given
        BigDecimal positiveBalance = BigDecimal.ZERO;

        // When
        AccountId sourceAccountId = accountService.createNew(positiveBalance);

        // Then
        assertThat(sourceAccountId, notNullValue());
        assertThat(sourceAccountId.getValue(), not(isEmptyOrNullString()));
        verify(accountRepository, times(1)).save(eq(sourceAccountId), any(Account.class));
    }

    @Test
    public void shouldFailWhenCreateAccountWithNegativeBalance() {
        // Given
        BigDecimal positiveBalance = BigDecimal.ONE.negate();

        // When
        try {
            accountService.createNew(positiveBalance); // exception should be thrown
            fail("Expected exception to be thrown.");
        } catch (InvalidDataException expected) {
        }

        // Then
        verify(accountRepository, never()).save(any(AccountId.class), any(Account.class));
    }

    public static class TestModule extends AbstractModule {
        protected void configure() {
            // mock
            bind(AccountRepository.class).toInstance(mock(AccountRepository.class));
            bind(AccountValidator.class).to(DebitAccountValidator.class);
            bind(AccountService.class).to(InMemoryAccountService.class);
        }
    }
}
