package com.demo.api.account.service;

import com.demo.api.account.model.Account;
import com.demo.api.model.AccountId;
import com.demo.common.InvalidDataException;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AccountServiceTest {
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

    @Test
    public void shouldCreateAccountWithPositiveBalance() {
        // Given
        BigDecimal positiveBalance = BigDecimal.ONE;

        // When
        AccountId sourceAccountId = accountService.createNew(positiveBalance);

        // Then
        assertThat(sourceAccountId, notNullValue());
        assertThat(sourceAccountId.getValue(), not(isEmptyOrNullString()));
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
    }

    @Test(expected = InvalidDataException.class)
    public void shouldFailWhenCreateAccountWithNegativeBalance() {
        // Given
        BigDecimal positiveBalance = BigDecimal.ONE.negate();

        // When
        accountService.createNew(positiveBalance); // exception should be thrown
    }
}
