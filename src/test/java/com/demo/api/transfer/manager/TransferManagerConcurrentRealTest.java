package com.demo.api.transfer.manager;

import com.demo.api.account.AccountModule;
import com.demo.api.account.model.Account;
import com.demo.api.account.model.AccountId;
import com.demo.api.account.service.AccountService;
import com.demo.api.transfer.TransferModule;
import com.demo.api.transfer.model.Transfer;
import com.demo.api.transfer.model.TransferId;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules(TransferManagerConcurrentRealTest.TestModule.class)
public class TransferManagerConcurrentRealTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferManagerConcurrentRealTest.class);

    private static final long ASYNC_WAIT_IN_MS = 10000;
    private static final int BUFFER_SIZE = 10000;
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

    @Inject
    private TransferManager transferManager;

    @Inject
    private AccountService accountService;

    @Inject
    private TransferService transferService;

    @Inject
    private BootstrapConfig config;

    @Before
    public void init() {
        transferManager.start();
    }

    @After
    public void destroy() {
        transferManager.stop();
    }

    @Test(timeout = ASYNC_WAIT_IN_MS)
    public void shouldCompleteWithInitialBalanceIfHasCompensatedTransfer() throws Exception {
        // Given
        BigDecimal amount = BigDecimal.ONE;
        BigDecimal initialBalance = BigDecimal.valueOf(config.getBufferSize());
        AccountId sourceAccountId = accountService.createNew(initialBalance);
        AccountId targetAccountId = accountService.createNew(initialBalance);

        // When
        List<Future<TransferId>> generatedTransfers = generateTransfers(amount, sourceAccountId, targetAccountId);

        // Then
        int numOfCompletedTransfers = waitForAllTransferTasksToComplete(generatedTransfers);
        assertThat(generatedTransfers.size(), equalTo(numOfCompletedTransfers));
        validateAccountBalance(sourceAccountId, initialBalance);
        validateAccountBalance(targetAccountId, initialBalance);
    }

    private int waitForAllTransferTasksToComplete(List<Future<TransferId>> transferTasks) {
        AtomicInteger completed = new AtomicInteger(0);
        transferTasks.parallelStream()
                .forEach(transferTask -> {
                    try {
                        TransferId transferId = transferTask.get();
                        waitForTransferToComplete(transferId);
                        completed.incrementAndGet();
                    } catch (InterruptedException | ExecutionException e) {
                        LOGGER.error("Error retrieving result", e);
                        throw new RuntimeException(e);
                    }
                });

        return completed.get();
    }

    private void waitForTransferToComplete(TransferId transferId) {
        Transfer transfer = null;
        while (transfer == null) {
            transfer = transferService.findById(transferId);

            if (transfer != null) {
                if (TransferState.ERROR == transfer.getState()) {
                    throw new RuntimeException("Transfer not valid");
                }
                if (TransferState.DONE == transfer.getState()) {
                    return;
                }
            }
        }
    }

    private List<Future<TransferId>> generateTransfers(BigDecimal amount,
                                                       AccountId sourceAccountId,
                                                       AccountId targetAccountId) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(config.getMaxThreads());
        List<Future<TransferId>> futures = new ArrayList<>();
        int requestsPairs = config.getBufferSize() / 2;
        for (int i = 0; i < requestsPairs; i++) {
            // accounts: A, B
            // 1 req - A -> B
            // 2 req - B -> A (compensate)
            futures.add(executor.submit(() -> transferService.createNew(sourceAccountId, targetAccountId, amount)));
            futures.add(executor.submit(() -> transferService.createNew(targetAccountId, sourceAccountId, amount)));
        }
        executor.shutdown();
        executor.awaitTermination(ASYNC_WAIT_IN_MS, TimeUnit.MILLISECONDS);

        return futures;
    }

    private void validateAccountBalance(AccountId accountId, BigDecimal expectedBalance) {
        Account account = accountService.findById(accountId);
        assertThat(account.getBalance(), equalTo(expectedBalance));
    }

    public static class TestModule extends AbstractModule {
        protected void configure() {
            BootstrapConfig config = BootstrapConfig.builder()
                    .withBufferSize(BUFFER_SIZE)
                    .withMaxThreads(MAX_THREADS)
                    .build();
            bind(BootstrapConfig.class).toInstance(config);
            install(new AccountModule());
            install(new TransferModule(config));
        }
    }
}
