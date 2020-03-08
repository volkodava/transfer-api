package com.demo.api.transfer.manager;

import com.demo.api.account.repository.AccountRepository;
import com.demo.api.transfer.model.TransferEvent;
import com.demo.api.transfer.repository.TransferRepository;
import com.demo.api.transfer.store.EventStore;
import com.demo.api.transfer.validator.DebitTransferValidator;
import com.demo.common.BootstrapConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Singleton
public class InMemoryTransferManager implements TransferManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryTransferManager.class);

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final DebitTransferValidator transferValidator;
    private final EventStore<TransferEvent> eventSource;
    private PipelineExecutor pipeline;
    @Inject
    private BootstrapConfig config;

    @Inject
    public InMemoryTransferManager(TransferRepository transferRepository,
                                   AccountRepository accountRepository,
                                   DebitTransferValidator transferValidator,
                                   EventStore<TransferEvent> eventSource) {
        this.transferRepository = Objects.requireNonNull(transferRepository, "Transfer repository must be provided");
        this.accountRepository = Objects.requireNonNull(accountRepository, "Account repository must be provided");
        this.transferValidator = Objects.requireNonNull(transferValidator, "Transfer validator must be provided");
        this.eventSource = Objects.requireNonNull(eventSource, "Event source must be provided");
    }

    @Override
    public boolean submitEvent(TransferEvent event) {
        return eventSource.put(event);
    }

    @Override
    public void start() {
        pipeline = PipelineExecutor.builder()
                .withEventSource(eventSource)
                .withConfig(config)
                .withRegisterTransferFn(this::onRegisterTransfer)
                .withValidateTransferFn(this::onValidateTransfer)
                .withWithdrawSourceFn(this::onWithdrawSource)
                .withDepositTargetFn(this::onDepositTarget)
                .withFinalizeTransferFn(this::onFinalizeTransfer)
                .withCompleteTransferFn(this::onCompleteTransfer)
                .withErrorHandler(this::onError)
                .build();
        pipeline.start();
    }

    @Override
    public void stop() {
        pipeline.stop();
        eventSource.clear();
    }

    private TransferEvent onRegisterTransfer(TransferEvent event) {
        throw new UnsupportedOperationException();
    }

    private TransferEvent onValidateTransfer(TransferEvent event) {
        throw new UnsupportedOperationException();
    }

    private TransferEvent onWithdrawSource(TransferEvent event) {
        throw new UnsupportedOperationException();
    }

    private TransferEvent onDepositTarget(TransferEvent event) {
        throw new UnsupportedOperationException();
    }

    private TransferEvent onFinalizeTransfer(TransferEvent event) {
        throw new UnsupportedOperationException();
    }

    private void onCompleteTransfer(TransferEvent event) {
        throw new UnsupportedOperationException();
    }

    private void onError(Throwable throwable) {
        LOGGER.error("Fail to process stream of transfers", throwable);
    }
}
