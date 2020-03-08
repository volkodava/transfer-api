package com.demo.api.transfer.manager;

import com.demo.api.account.AccountModule;
import com.demo.api.account.model.Account;
import com.demo.api.account.service.AccountService;
import com.demo.api.model.AccountId;
import com.demo.api.model.TransferId;
import com.demo.api.transfer.TransferModule;
import com.demo.api.transfer.model.Transfer;
import com.demo.api.transfer.model.TransferState;
import com.demo.api.transfer.service.TransferService;
import com.demo.common.BootstrapConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import net.lamberto.junit.GuiceJUnitRunner;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules(TransferManagerConcurrentRealTest.TestModule.class)
public class TransferManagerConcurrentRealTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferManagerConcurrentRealTest.class);

    private static final long ASYNC_WAIT_IN_MS = 10000;
    private static final int PARALLELISM_LEVEL = 8;
    private static final int BUFFER_SIZE = 600;
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int DATA_PAIRS = BUFFER_SIZE / 2;

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
        Awaitility.setDefaultTimeout(Duration.ofMillis(ASYNC_WAIT_IN_MS));

        transferManager.start();
    }

    @After
    public void destroy() {
        transferManager.stop();
    }

    @Test
    public void shouldCompleteWithInitialBalanceIfHasCompensatedTransfer() throws Exception {
        // Given
        BigDecimal amount = BigDecimal.ONE;
        BigDecimal initialBalance = BigDecimal.valueOf(config.getBufferSize());
        AccountId sourceAccountId = accountService.createNew(initialBalance);
        AccountId targetAccountId = accountService.createNew(initialBalance);

        // When
        List<Future<TransferId>> generatedTransfers = generateTransfers(amount, sourceAccountId, targetAccountId);

        // Then
        int numOfCompleted = waitForAllTransfersCondition(generatedTransfers,
                transfer -> transfer != null && TransferState.DONE == transfer.getState());
        assertThat(generatedTransfers.size(), equalTo(numOfCompleted));
        validateAccountBalance(sourceAccountId, initialBalance);
        validateAccountBalance(targetAccountId, initialBalance);
    }

    private int waitForAllTransfersCondition(List<Future<TransferId>> futures,
                                             Predicate<Transfer> filter) {
        AtomicInteger counter = new AtomicInteger(0);
        futures.parallelStream()
                .forEach(future -> {
                    try {
                        TransferId transferId = future.get();
                        await().until(() -> {
                            Transfer transfer = transferService.findById(transferId);
                            return filter.test(transfer);
                        });
                        counter.incrementAndGet();
                    } catch (Exception e) {
                        LOGGER.error("Error retrieving result", e);
                        throw new RuntimeException(e);
                    }
                });

        return counter.get();
    }

    private List<Future<TransferId>> generateTransfers(BigDecimal amount, AccountId sourceAccountId, AccountId targetAccountId) {
        ExecutorService executor = Executors.newFixedThreadPool(PARALLELISM_LEVEL);
        List<Future<TransferId>> futures = new ArrayList<>();
        for (int i = 0; i < DATA_PAIRS; i++) {
            // deposit target
            futures.add(executor.submit(() -> transferService.createNew(sourceAccountId, targetAccountId, amount)));
            // respective compensate transfer - deposit source
            futures.add(executor.submit(() -> transferService.createNew(targetAccountId, sourceAccountId, amount)));
        }
        executor.shutdown();

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
