package com.demo.api.account.service;

import com.demo.api.account.AccountModule;
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

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules(AccountServiceTest.TestModule.class)
public class AccountServiceTest {

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

    public static class TestModule extends AbstractModule {
        protected void configure() {
            install(new AccountModule());
        }
    }
}
