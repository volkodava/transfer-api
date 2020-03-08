package com.demo.api.transfer.manager;

import com.demo.api.account.AccountModule;
import com.demo.api.account.model.Account;
import com.demo.api.account.service.AccountService;
import com.demo.api.model.AccountId;
import com.demo.api.model.TransferId;
import com.demo.api.transfer.TransferModule;
import com.demo.api.transfer.model.Transfer;
import com.demo.api.transfer.model.TransferEvent;
import com.demo.api.transfer.model.TransferState;
import com.demo.api.transfer.service.TransferService;
import com.demo.common.BootstrapConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import net.lamberto.junit.GuiceJUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.function.Predicate;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules(TransferManagerRealTest.TestModule.class)
public class TransferManagerRealTest {

    @Inject
    private TransferManager transferManager;

    @Inject
    private TransferService transferService;

    @Inject
    private AccountService accountService;

    @Inject
    private BootstrapConfig config;

    @Before
    public void init() {
        // tests rely on buffer with size 1
        assertThat(1, equalTo(config.getBufferSize()));
    }

    @After
    public void destroy() {
        transferManager.stop();
    }

    @Test
    public void shouldExecuteTransferIfBelowThreshold() {
        // Given
        transferManager.start();
        BigDecimal amount = BigDecimal.ONE;
        BigDecimal sourceInitialBalance = BigDecimal.TEN;
        BigDecimal targetInitialBalance = BigDecimal.ZERO;
        AccountId sourceAccountId = accountService.createNew(sourceInitialBalance);
        AccountId targetAccountId = accountService.createNew(targetInitialBalance);
        TransferEvent transferEvent = TransferEvent.builder()
                .withTransferId(TransferId.valueOf("1"))
                .withSourceId(sourceAccountId)
                .withTargetId(targetAccountId)
                .withAmount(amount)
                .withState(TransferState.NEW)
                .withDetails("Transfer created")
                .build();

        // When
        transferManager.submitEvent(transferEvent);

        // Then
        waitForTransferCondition(transferEvent.getTransferId(),
                transfer -> transfer != null && transfer.getState() == TransferState.DONE);
        validateAccountBalance(sourceAccountId, sourceInitialBalance.subtract(amount));
        validateAccountBalance(targetAccountId, targetInitialBalance.add(amount));
    }

    @Test
    public void shouldRejectTransferIfAboveThreshold() {
        // Given
        AccountId sourceAccountId = accountService.createNew(BigDecimal.TEN);
        AccountId targetAccountId = accountService.createNew(BigDecimal.ZERO);
        BigDecimal amount = BigDecimal.ONE;
        TransferEvent transferEvent1 = TransferEvent.builder()
                .withTransferId(TransferId.valueOf("1"))
                .withSourceId(sourceAccountId)
                .withTargetId(targetAccountId)
                .withAmount(amount)
                .withState(TransferState.NEW)
                .withDetails("Transfer created")
                .build();
        TransferEvent transferEvent2 = TransferEvent.builder()
                .withTransferId(TransferId.valueOf("1"))
                .withSourceId(sourceAccountId)
                .withTargetId(targetAccountId)
                .withAmount(amount)
                .withState(TransferState.NEW)
                .withDetails("Transfer created")
                .build();

        // When
        boolean submitted1 = transferManager.submitEvent(transferEvent1);
        boolean submitted2 = transferManager.submitEvent(transferEvent2);
        transferManager.start();

        // Then
        assertThat(submitted1, is(true));
        assertThat(submitted2, is(false));
    }

    private void validateAccountBalance(AccountId accountId, BigDecimal expectedBalance) {
        Account account = accountService.findById(accountId);
        assertThat(account.getBalance(), equalTo(expectedBalance));
    }

    private void waitForTransferCondition(TransferId transferId,
                                          Predicate<Transfer> filter) {
        await().until(() -> {
            Transfer transfer = transferService.findById(transferId);
            return filter.test(transfer);
        });
    }

    public static class TestModule extends AbstractModule {
        protected void configure() {
            BootstrapConfig config = BootstrapConfig.builder()
                    .withBufferSize(1)
                    .withMaxThreads(1)
                    .build();
            bind(BootstrapConfig.class).toInstance(config);
            install(new AccountModule());
            install(new TransferModule(config));
        }
    }
}
